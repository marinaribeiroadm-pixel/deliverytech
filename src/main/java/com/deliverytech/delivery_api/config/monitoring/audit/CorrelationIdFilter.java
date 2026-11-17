package com.deliverytech.delivery_api.config.monitoring.audit;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Adiciona um Correlation ID por requisição (header X-Correlation-ID) e dados
 * de contexto ao MDC para enriquecer os logs. Não cria sessão HTTP.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorrelationIdFilter extends OncePerRequestFilter {

    private static final String CORRELATION_ID_HEADER = "X-Correlation-ID";

    private static final String KEY_CORRELATION_ID = "correlationId";
    private static final String KEY_CLIENT_IP = "clienteIp";
    private static final String KEY_USER_AGENT = "userAgent";
    private static final String KEY_REQUEST_URI = "requestUri";
    private static final String KEY_HTTP_METHOD = "httpMethod";

    private static final List<String> MDC_KEYS = List.of(
        KEY_CORRELATION_ID, KEY_CLIENT_IP, KEY_USER_AGENT, KEY_REQUEST_URI, KEY_HTTP_METHOD
    );

    // Aceita apenas IDs simples, evitando log injection via header
    private static final Pattern VALID_ID = Pattern.compile("^[A-Za-z0-9-]{1,64}$");
    private static final int MAX_USER_AGENT_LENGTH = 200;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {

        String correlationId = resolveCorrelationId(request.getHeader(CORRELATION_ID_HEADER));
        response.setHeader(CORRELATION_ID_HEADER, correlationId);

        try {
            MDC.put(KEY_CORRELATION_ID, correlationId);
            MDC.put(KEY_CLIENT_IP, request.getRemoteAddr());
            MDC.put(KEY_USER_AGENT, sanitize(request.getHeader("User-Agent")));
            MDC.put(KEY_REQUEST_URI, request.getRequestURI());
            MDC.put(KEY_HTTP_METHOD, request.getMethod());

            chain.doFilter(request, response); // repassa a requisição adiante
        } finally {
            // Remove só as chaves deste filtro, preservando traceId/spanId do tracing
            MDC_KEYS.forEach(MDC::remove);
        }
    }

    private String resolveCorrelationId(String header) {
        if (header != null && VALID_ID.matcher(header).matches()) {
            return header;
        }
        return UUID.randomUUID().toString();
    }

    
    /** Remove quebras de linha e limita o tamanho para evitar log injection. */
    private String sanitize(String value) {
        if (value == null) {
            return "-";
        }
        String clean = value.replaceAll("[\\r\\n\\t]", "_");
        return clean.length() > MAX_USER_AGENT_LENGTH
            ? clean.substring(0, MAX_USER_AGENT_LENGTH)
            : clean;
    }
}
