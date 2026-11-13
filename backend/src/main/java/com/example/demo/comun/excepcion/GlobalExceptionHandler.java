package com.example.demo.comun.excepcion;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/** Atrapa las excepciones de TODOS los controllers y las convierte en JSON uniforme. */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorApi> validacion(MethodArgumentNotValidException ex) {
        Map<String, String> detalles = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(e -> detalles.putIfAbsent(e.getField(), e.getDefaultMessage()));
        return responder(HttpStatus.BAD_REQUEST, "Datos inválidos",
                "Revisa los campos enviados", detalles);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorApi> jsonMalFormado(HttpMessageNotReadableException ex) {
        return responder(HttpStatus.BAD_REQUEST, "Petición inválida",
                "El cuerpo de la petición no es un JSON válido", null);
    }

    @ExceptionHandler(EmailYaRegistradoException.class)
    public ResponseEntity<ErrorApi> emailDuplicado(EmailYaRegistradoException ex) {
        return responder(HttpStatus.CONFLICT, "Conflicto", ex.getMessage(), null);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorApi> integridad(DataIntegrityViolationException ex) {
        log.warn("Violación de integridad: {}", ex.getMostSpecificCause().getMessage());
        return responder(HttpStatus.CONFLICT, "Conflicto",
                "Los datos enviados entran en conflicto con datos existentes", null);
    }

    // Mensaje genérico a propósito: no revelamos si falló el email o la contraseña
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorApi> credenciales(AuthenticationException ex) {
        return responder(HttpStatus.UNAUTHORIZED, "No autenticado",
                "Credenciales inválidas", null);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorApi> accesoDenegado(AccessDeniedException ex) {
        return responder(HttpStatus.FORBIDDEN, "Acceso denegado",
                "No tienes permisos para esta acción", null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorApi> generico(Exception ex) {
        // Errores propios de Spring (404, 405, etc.) ya traen su código correcto
        if (ex instanceof org.springframework.web.ErrorResponse er) {
            return responder(er.getStatusCode(), "Error", "No se pudo procesar la petición", null);
        }
        log.error("Error inesperado", ex);   // el detalle va al log, NO al cliente
        return responder(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno",
                "Ocurrió un error inesperado", null);
    }

    private ResponseEntity<ErrorApi> responder(HttpStatusCode estado, String error,
                                               String mensaje, Map<String, String> detalles) {
        return ResponseEntity.status(estado)
                .body(new ErrorApi(LocalDateTime.now(), estado.value(), error, mensaje, detalles));
    }
}
