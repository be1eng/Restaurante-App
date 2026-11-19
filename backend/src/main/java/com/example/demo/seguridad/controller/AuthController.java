package com.example.demo.seguridad.controller;

import com.example.demo.seguridad.dto.AuthResponse;
import com.example.demo.seguridad.dto.LoginRequest;
import com.example.demo.seguridad.dto.RegistroRequest;
import com.example.demo.seguridad.dto.RegistroRestauranteRequest;
import com.example.demo.seguridad.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/registro")
    public ResponseEntity<AuthResponse> registro(@Valid @RequestBody RegistroRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.registrar(request));
    }

    /** Registra al dueño y su restaurante en una sola petición. */
    @PostMapping("/registro-restaurante")
    public ResponseEntity<AuthResponse> registroRestaurante(@Valid @RequestBody RegistroRestauranteRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.registrarRestaurante(request));
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }
}
