package com.example.demo.restaurante.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RestauranteRequest(
        @NotBlank(message = "El nombre del restaurante es obligatorio")
        @Size(max = 150, message = "El nombre no puede superar 150 caracteres")
        String nombre,

        @NotBlank(message = "La dirección es obligatoria")
        @Size(max = 255, message = "La dirección no puede superar 255 caracteres")
        String direccion,

        @Size(max = 30, message = "El teléfono no puede superar 30 caracteres")
        String telefono
) {
}
