package com.deliverytech.delivery_api.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

@Service
public class AuditService {

    /*
        Classe responsável por registrar eventos de auditoria no sistema de entrega.
        Ela utiliza o SLF4J para logar eventos em formato JSON, incluindo informações como
        timestamp, userId, action, resource, detalhes, correlationId e sessionId.
        O método logUserAction é chamado sempre que uma ação relevante do usuário ocorre,
        permitindo rastrear atividades e manter um histórico de auditoria.
    */
  private static final Logger auditLogger = LoggerFactory.getLogger("AUDIT");
  private final ObjectMapper objectMapper = new ObjectMapper();

    /*
        Classe public AuditService é responsável por registrar eventos de auditoria 
        no sistema de entrega.
        Ela utiliza o SLF4J para logar eventos em formato JSON, incluindo informações como
        timestamp, userId, action, resource, detalhes, correlationId e sessionId.
        O método logUserAction é chamado sempre que uma ação relevante do usuário ocorre,
        permitindo rastrear atividades e manter um histórico de auditoria.
    */
  public void logUserAction(
    String userId,
    String action,
    String resource,
    Object details
  ) {
    try {
        /*
        O método logUserAction registra eventos de auditoria no sistema de entrega.
        Ele cria um mapa de eventos contendo informações como timestamp, userId, action,
        resource, detalhes, correlationId e sessionId. Em seguida, converte o mapa em JSON
        e registra o evento usando o logger de auditoria.
        */
      Map<String, Object> auditEvent = new HashMap<>();
      auditEvent.put("timestamp", LocalDateTime.now().toString());
      auditEvent.put("userId", userId);
      auditEvent.put("action", action);
      auditEvent.put("resource", resource);
      auditEvent.put("details", details);
      /*
        O uso do MDC (Mapped Diagnostic Context) permite associar informações de contexto,
        como correlationId e sessionId, aos logs, facilitando a rastreabilidade e análise
        de eventos no sistema.
      */
      auditEvent.put("correlationId", MDC.get("correlationId"));
      auditEvent.put("sessionId", MDC.get("sessionId"));

      String jsonLog = objectMapper.writeValueAsString(auditEvent);
      auditLogger.info(jsonLog);
    } catch (Exception e) {
      auditLogger.error("Erro ao registrar evento de auditoria", e);
    }
  }

    /*
        Classe responsável por registrar mudanças de dados no sistema de entrega.
        O método logDataChange é chamado sempre que uma entidade do sistema é alterada,
        permitindo rastrear alterações e manter um histórico de auditoria.
        Ele registra informações como timestamp, userId, entity, entityId, oldValue,
        newValue, operation e correlationId.
    */
  public void logDataChange(
    String userId,
    String entity,
    String entityId,
    Object oldValue,
    Object newValue,
    String operation
  ) {
    try {
      Map<String, Object> changeEvent = new HashMap<>();
      changeEvent.put("timestamp", LocalDateTime.now().toString());
      changeEvent.put("userId", userId);
      changeEvent.put("entity", entity);
      changeEvent.put("entityId", entityId);
      changeEvent.put("operation", operation);
      changeEvent.put("oldValue", oldValue);
      changeEvent.put("newValue", newValue);
      changeEvent.put("correlationId", MDC.get("correlationId"));

      String jsonLog = objectMapper.writeValueAsString(changeEvent);
      auditLogger.info(jsonLog);
    } catch (Exception e) {
      auditLogger.error("Erro ao registrar mudança de dados", e);
    }
  }

    /*
        Classe responsável por registrar eventos de segurança no sistema de entrega.
        O método logSecurityEvent é chamado sempre que um evento de segurança relevante ocorre,
        permitindo rastrear atividades suspeitas e manter um histórico de auditoria.
        Ele registra informações como timestamp, userId, eventType, description e correlationId.
    */
  public void logSecurityEvent(
    String userId,
    String event,
    String details,
    boolean success
  ) {
    try {
      Map<String, Object> securityEvent = new HashMap<>();
      securityEvent.put("timestamp", LocalDateTime.now().toString());
      securityEvent.put("userId", userId);
      securityEvent.put("event", event);
      securityEvent.put("details", details);
      securityEvent.put("success", success);
      securityEvent.put("correlationId", MDC.get("correlationId"));
      securityEvent.put("ipAddress", MDC.get("clientIp"));
      securityEvent.put("userAgent", MDC.get("userAgent"));

      String jsonLog = objectMapper.writeValueAsString(securityEvent);
      auditLogger.info(jsonLog);
    } catch (Exception e) {
      auditLogger.error("Erro ao registrar evento de segurança", e);
    }
  }

}
