package com.estudiobiblico.exception;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import org.springframework.security.authentication.BadCredentialsException;


@RestControllerAdvice
public class GlobalExceptionHandler {

        @ExceptionHandler(BadCredentialsException.class)
        public ResponseEntity<Map<String, String>> manejarCredencialesIncorrectas(
                BadCredentialsException ex) {

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(Map.of(
                        "error", "Unauthorized",
                        "mensaje", "Correo o contraseña incorrectos"
                ));
        }

        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<Map<String, Object>> manejarValidacion(
                MethodArgumentNotValidException ex) {

                Map<String, String> errores = ex.getBindingResult()
                        .getFieldErrors()
                        .stream()
                        .collect(Collectors.toMap(
                                FieldError::getField,
                                error -> error.getDefaultMessage() != null
                                        ? error.getDefaultMessage()
                                        : "Valor inválido",
                                (mensajeAnterior, mensajeNuevo) -> mensajeAnterior,
                                LinkedHashMap::new
                        ));

                Map<String, Object> respuesta = new LinkedHashMap<>();
                respuesta.put("error", "Bad Request");
                respuesta.put("mensaje", "Los datos enviados no son válidos");
                respuesta.put("detalles", errores);

                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(respuesta);
        }

        @ExceptionHandler(HttpMessageNotReadableException.class)
        public ResponseEntity<Map<String, Object>> manejarJsonInvalido(
                HttpMessageNotReadableException ex) {
        Throwable causa = ex;
        while (causa != null) {
                if (causa instanceof IllegalArgumentException
                        && causa.getMessage() != null
                        && causa.getMessage().contains(
                                "El versículo hasta no puede ser menor que el versículo desde")) {

                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(Map.of(
                                "error", "Bad Request",
                                "mensaje", causa.getMessage()
                        ));
                }

                causa = causa.getCause();
        }

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of(
                        "error", "Bad Request",
                        "mensaje", "El JSON enviado es inválido o tiene tipos de datos incorrectos"
                ));
        }

        @ExceptionHandler(IllegalArgumentException.class)
        public ResponseEntity<Map<String, Object>> manejarArgumentoInvalido(
                IllegalArgumentException ex) {

                String mensaje = ex.getMessage() != null
                        ? ex.getMessage()
                        : "Los argumentos enviados no son válidos";

                if ("Tema no encontrado".equals(mensaje)) {
                        return ResponseEntity
                                .status(HttpStatus.NOT_FOUND)
                                .body(Map.of(
                                        "error", "Not Found",
                                        "mensaje", mensaje
                                ));
                }

                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(Map.of(
                                "error", "Bad Request",
                                "mensaje", mensaje
                        ));
        }

        @ExceptionHandler(IllegalStateException.class)
        public ResponseEntity<Map<String, Object>> manejarEstadoInvalido(
                IllegalStateException ex) {

                return ResponseEntity
                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of(
                                "error", "Internal Server Error",
                                "mensaje", ex.getMessage() != null
                                        ? ex.getMessage()
                                        : "Se produjo un error interno"
                        ));
        }  
}