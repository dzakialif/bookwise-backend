package com.dzaki.bookwise.exception;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import com.dzaki.bookwise.model.WebResponse;

import jakarta.validation.ConstraintViolationException;

@RestControllerAdvice
public class ApiResponseAdvice implements ResponseBodyAdvice<Object> {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<WebResponse<Object>> handleNotFound(ResourceNotFoundException ex) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, List.of(ex.getMessage()));
    }

    @ExceptionHandler(ResourceAlreadyExistsException.class)
    public ResponseEntity<WebResponse<Object>> handleConflict(ResourceAlreadyExistsException ex) {
        return buildErrorResponse(HttpStatus.CONFLICT, List.of(ex.getMessage()));
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<WebResponse<Object>> handleBadRequest(BadRequestException ex) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, List.of(ex.getMessage()));
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<WebResponse<Object>> handleUnauthorized(UnauthorizedException ex) {
        return buildErrorResponse(HttpStatus.UNAUTHORIZED, List.of(ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<WebResponse<Object>> handleValidation(MethodArgumentNotValidException ex) {
        List<String> errors = ex.getBindingResult().getFieldErrors().stream()
            .map(FieldError::getDefaultMessage)
            .collect(Collectors.toList());
        return buildErrorResponse(HttpStatus.BAD_REQUEST, errors);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<WebResponse<Object>> handleConstraintViolation(ConstraintViolationException ex) {
        List<String> errors = ex.getConstraintViolations().stream()
            .map(violation -> violation.getMessage())
            .collect(Collectors.toList());
        return buildErrorResponse(HttpStatus.BAD_REQUEST, errors);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<WebResponse<Object>> handleUnexpected(Exception ex) {
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, List.of("Unexpected error"));
    }

    private ResponseEntity<WebResponse<Object>> buildErrorResponse(HttpStatus status, List<String> errors) {
        WebResponse<Object> body = WebResponse.builder()
            .message(mapStatusMessage(status))
            .errors(errors)
            .build();
        return ResponseEntity.status(status).contentType(MediaType.APPLICATION_JSON).body(body);
    }

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
            Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request,
            ServerHttpResponse response) {
        if (!(body instanceof WebResponse)) {
            return body;
        }

        WebResponse<?> webResponse = (WebResponse<?>) body;
        if (webResponse.getMessage() != null && !webResponse.getMessage().isBlank()) {
            return body;
        }

        HttpStatus status = HttpStatus.OK;
        if (response instanceof ServletServerHttpResponse servletResponse) {
            int rawStatus = servletResponse.getServletResponse().getStatus();
            HttpStatus resolved = HttpStatus.resolve(rawStatus);
            if (resolved != null) {
                status = resolved;
            }
        }

        webResponse.setMessage(mapStatusMessage(status));
        return webResponse;
    }

    private String mapStatusMessage(HttpStatus status) {
        return switch (status) {
            case OK -> "Success";
            case CREATED -> "Created";
            case BAD_REQUEST -> "Bad Request";
            case UNAUTHORIZED -> "Unauthorized";
            case FORBIDDEN -> "Forbidden";
            case NOT_FOUND -> "Not Found";
            case CONFLICT -> "Conflict";
            case INTERNAL_SERVER_ERROR -> "Internal Server Error";
            default -> status.getReasonPhrase();
        };
    }
}
