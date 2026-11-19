package com.example.demo.seguridad.dto;

import com.example.demo.restaurante.dto.RestauranteRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

/** NUEVO: registra al dueño y su restaurante en una sola petición. */
public record RegistroRestauranteRequest(
        @NotNull(message = "Los datos del propietario son obligatorios")
        @Valid
        RegistroRequest propietario,

        @NotNull(message = "Los datos del restaurante son obligatorios")
        @Valid
        RestauranteRequest restaurante
) {
}
