package com.example.demo.comun.excepcion;

import java.time.LocalDateTime;
import java.util.Map;

/** Formato único de TODOS los errores que devuelve la API. */
public record ErrorApi(
        LocalDateTime fecha,
        int estado,
        String error,
        String mensaje,
        Map<String, String> detalles
) {}
