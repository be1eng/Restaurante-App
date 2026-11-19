package com.example.demo.restaurante.dto;

import com.example.demo.restaurante.entity.Restaurante;

import java.time.LocalDateTime;

public record RestauranteResponse(
        Long id,
        String nombre,
        String direccion,
        String telefono,
        boolean activo,
        LocalDateTime creadoAt
) {
    public static RestauranteResponse desde(Restaurante r) {
        return new RestauranteResponse(
                r.getId(), r.getNombre(), r.getDireccion(), r.getTelefono(), r.isActivo(), r.getCreadoAt());
    }
}
