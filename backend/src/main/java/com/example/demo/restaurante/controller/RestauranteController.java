package com.example.demo.restaurante.controller;

import com.example.demo.comun.dto.PaginaResponse;
import com.example.demo.restaurante.dto.EstadoRequest;
import com.example.demo.restaurante.dto.RestauranteRequest;
import com.example.demo.restaurante.dto.RestauranteResponse;
import com.example.demo.restaurante.service.RestauranteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/restaurantes")
@RequiredArgsConstructor
public class RestauranteController {

    private final RestauranteService restauranteService;

    // ----- Dueño -----

    @GetMapping("/mi-restaurante")
    @PreAuthorize("hasAuthority('RESTAURANTE_LEER')")
    public RestauranteResponse miRestaurante(Authentication authentication) {
        return restauranteService.obtenerMiRestaurante(authentication.getName());
    }

    @PutMapping("/mi-restaurante")
    @PreAuthorize("hasAuthority('RESTAURANTE_ESCRIBIR')")
    public RestauranteResponse actualizarMiRestaurante(Authentication authentication,
                                                       @Valid @RequestBody RestauranteRequest request) {
        return restauranteService.actualizarMiRestaurante(authentication.getName(), request);
    }

    // ----- Administrador -----

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public PaginaResponse<RestauranteResponse> listar(@PageableDefault(size = 10, sort = "nombre") Pageable pageable) {
        return restauranteService.listar(pageable);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public RestauranteResponse obtener(@PathVariable Long id) {
        return restauranteService.obtener(id);
    }

    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasRole('ADMIN')")
    public RestauranteResponse cambiarEstado(@PathVariable Long id, @Valid @RequestBody EstadoRequest request) {
        return restauranteService.cambiarEstado(id, request.activo());
    }
}
