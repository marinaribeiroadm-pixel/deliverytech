package com.deliverytech.delivery_api.exeption;

import com.deliverytech.delivery_api.dto.ErrorResponse;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.method.ParameterErrors;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Tratamento centralizado de exceções da API.
 *
 * Regras:
 * - Cada tipo de erro conhecido recebe o status HTTP correto.
 * - O handler genérico (500) registra o stack trace em log, mas devolve
 *   ao cliente apenas uma mensagem genérica (sem expor detalhes internos).
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ---------------------------------------------------------------
    // Erros de domínio
    // ---------------------------------------------------------------

    /** Exceção de domínio: recurso não encontrado (404). */
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEntityNotFound(
            EntityNotFoundException ex, HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, "Entidade não encontrada", ex.getMessage(), request);
    }

    /**
     * Exceção do JPA com o mesmo nome simples da classe de domínio
     * (lançada, por exemplo, por getReferenceById). Nome qualificado para evitar colisão.
     */
    @ExceptionHandler(jakarta.persistence.EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleJpaEntityNotFound(
            jakarta.persistence.EntityNotFoundException ex, HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, "Entidade não encontrada",
                "O recurso solicitado não foi encontrado.", request);
    }

    /** Violação de regra de negócio (400). */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(
            BusinessException ex, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, "Erro de regra de negócio", ex.getMessage(), request);
    }

    // ---------------------------------------------------------------
    // Erros de validação e de requisição
    // ---------------------------------------------------------------

    /** Falha no @Valid do corpo da requisição (400), com detalhes por campo. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        // LinkedHashMap mantém a ordem; putIfAbsent preserva o primeiro erro de cada campo.
        // getFieldErrors() evita ClassCastException com erros globais (ObjectError).
        Map<String, String> errors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(error -> errors.putIfAbsent(error.getField(), error.getDefaultMessage()));

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Dados inválidos",
                "Ocorreu um erro na validação dos campos enviados.",
                request.getRequestURI()
        );
        errorResponse.setDetails(errors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /** JSON malformado ou corpo ausente (400). */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleNotReadable(
            HttpMessageNotReadableException ex, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, "Requisição malformada",
                "Corpo da requisição ausente ou inválido.", request);
    }

    /** Tipo inválido em path variable ou parâmetro, ex.: /api/clientes/abc (400). */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        String message = String.format("Valor inválido para o parâmetro '%s'.", ex.getName());
        return build(HttpStatus.BAD_REQUEST, "Parâmetro inválido", message, request);
    }

    /** Parâmetro obrigatório de query ausente (400). */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParameter(
            MissingServletRequestParameterException ex, HttpServletRequest request) {
        String message = String.format("Parâmetro obrigatório '%s' não informado.", ex.getParameterName());
        return build(HttpStatus.BAD_REQUEST, "Parâmetro ausente", message, request);
    }

    /** Rota inexistente (404). */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResource(
            NoResourceFoundException ex, HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, "Recurso não encontrado",
                "O endpoint solicitado não existe.", request);
    }

    /** Método HTTP não suportado pelo endpoint (405). */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotSupported(
            HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {
        String message = String.format("Método '%s' não é suportado para este endpoint.", ex.getMethod());
        return build(HttpStatus.METHOD_NOT_ALLOWED, "Método não suportado", message, request);
    }

    /** Content-Type não suportado (415). */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMediaTypeNotSupported(
            HttpMediaTypeNotSupportedException ex, HttpServletRequest request) {
        return build(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Tipo de conteúdo não suportado",
                "Utilize Content-Type: application/json.", request);
    }

    // ---------------------------------------------------------------
    // Segurança e persistência
    // ---------------------------------------------------------------

    /**
     * Acesso negado por segurança de método (@PreAuthorize) (403).
     * Sem este handler, o handler genérico transformaria o 403 em 500.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(
            AccessDeniedException ex, HttpServletRequest request) {
        return build(HttpStatus.FORBIDDEN, "Acesso negado",
                "Você não tem permissão para realizar esta operação.", request);
    }

    /**
     * Violação de restrição do banco, ex.: email duplicado em coluna UNIQUE (409).
     * A mensagem original pode conter SQL e nomes de tabelas, por isso vai só para o log.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrity(
            DataIntegrityViolationException ex, HttpServletRequest request) {
        log.warn("Violação de integridade em {}: {}", request.getRequestURI(),
                ex.getMostSpecificCause().getMessage());
        return build(HttpStatus.CONFLICT, "Conflito de dados",
                "A operação viola uma restrição de dados (ex.: registro duplicado).", request);
    }


    /** Qualquer erro não previsto (500). Registra o stack trace e não expõe detalhes. */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex, HttpServletRequest request) {
        log.error("Erro não tratado em {} {}", request.getMethod(), request.getRequestURI(), ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno do servidor",
                "Ocorreu um erro inesperado. Tente novamente mais tarde.", request);
    }

    private ResponseEntity<ErrorResponse> build(
            HttpStatus status, String error, String message, HttpServletRequest request) {
        ErrorResponse body = new ErrorResponse(status.value(), error, message, request.getRequestURI());
        return ResponseEntity.status(status).body(body);
    }

        @ExceptionHandler(HandlerMethodValidationException.class)
        public ResponseEntity<Map<String, Object>> handleMethodValidation(HandlerMethodValidationException ex) {
            Map<String, String> details = new LinkedHashMap<>();

            ex.getParameterValidationResults().forEach(result -> {
                if (result instanceof ParameterErrors errors) {
                    // Erros em objetos dentro de coleções: ex. itens[0].quantidade
                    String prefix = errors.getContainerIndex() != null
                            ? "itens[" + errors.getContainerIndex() + "]."
                            : "";
                    errors.getFieldErrors().forEach(fe ->
                            details.put(prefix + fe.getField(), fe.getDefaultMessage()));
                } else {
                    // Erros diretos no parâmetro (ex.: @NotEmpty na lista)
                    result.getResolvableErrors().forEach(err ->
                            details.put(result.getMethodParameter().getParameterName(), err.getDefaultMessage()));
                }
            });

            Map<String, Object> body = new LinkedHashMap<>();
            body.put("status", HttpStatus.BAD_REQUEST.value());
            body.put("message", "Dados inválidos");
            body.put("details", details);
            return ResponseEntity.badRequest().body(body);
        }
}
