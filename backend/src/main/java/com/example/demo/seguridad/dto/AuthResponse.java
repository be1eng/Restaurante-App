package com.example.demo.seguridad.dto;

/** Respuesta de registro/login: el token que el cliente debe enviar en cada petición. */
public record AuthResponse(String token, String tipo) {}
