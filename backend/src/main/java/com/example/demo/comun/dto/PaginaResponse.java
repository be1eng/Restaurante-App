package com.example.demo.comun.dto;

import org.springframework.data.domain.Page;

import java.util.List;

/** Respuesta paginada con formato propio y estable (no expone la estructura interna de Page). */
public record PaginaResponse<T>(List<T> contenido, int pagina, int tamano, long totalElementos, int totalPaginas) {

    public static <T> PaginaResponse<T> de(Page<T> page) {
        return new PaginaResponse<>(
                page.getContent(), page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
    }
}
