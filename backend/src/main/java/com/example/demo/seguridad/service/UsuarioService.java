package com.example.demo.seguridad.service;

import com.example.demo.seguridad.dto.UsuarioResponse;
import com.example.demo.seguridad.entity.Rol;
import com.example.demo.seguridad.entity.Usuario;
import com.example.demo.seguridad.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    @Transactional(readOnly = true)
    public UsuarioResponse obtenerPerfil(String email) {
        Usuario u = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));
        return new UsuarioResponse(
                u.getId(), u.getNombre(), u.getEmail(),
                u.getRoles().stream().map(Rol::getNombre).sorted().toList(),
                u.getRestaurante() != null ? u.getRestaurante().getId() : null);
    }
}
