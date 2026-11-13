package com.example.demo.seguridad.controller;

import com.example.demo.seguridad.dto.UsuarioResponse;
import com.example.demo.seguridad.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    /** Endpoint PROTEGIDO: sirve para comprobar que el token funciona. */
    @GetMapping("/me")
    public UsuarioResponse me(@AuthenticationPrincipal UserDetails principal) {
        return usuarioService.obtenerPerfil(principal.getUsername());
    }
}
