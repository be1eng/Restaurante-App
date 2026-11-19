package com.example.demo.seguridad.dto;

import java.util.List;

/** Datos del usuario que sí es seguro mostrar (sin contraseña). restauranteId es null si no es dueño. */
public record UsuarioResponse(Long id, String nombre, String email, List<String> roles, Long restauranteId) {}
