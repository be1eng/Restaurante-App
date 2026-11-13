package com.example.demo.seguridad.service;

import com.example.demo.comun.excepcion.EmailYaRegistradoException;
import com.example.demo.seguridad.dto.AuthResponse;
import com.example.demo.seguridad.dto.LoginRequest;
import com.example.demo.seguridad.dto.RegistroRequest;
import com.example.demo.seguridad.entity.Rol;
import com.example.demo.seguridad.entity.Usuario;
import com.example.demo.seguridad.jwt.JwtService;
import com.example.demo.seguridad.repository.RolRepository;
import com.example.demo.seguridad.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;
    private final JwtService jwtService;

    @Transactional
    public AuthResponse registrar(RegistroRequest req) {
        String email = normalizar(req.email());

        if (usuarioRepository.existsByEmail(email)) {
            throw new EmailYaRegistradoException();
        }

        Rol rolCliente = rolRepository.findByNombre("CLIENTE")
                .orElseThrow(() -> new IllegalStateException(
                        "No existe el rol CLIENTE. Revisa que V1__seguridad.sql se haya aplicado."));

        Usuario usuario = new Usuario();
        usuario.setNombre(req.nombre().trim());
        usuario.setEmail(email);
        usuario.setPasswordHash(passwordEncoder.encode(req.password()));  // NUNCA se guarda la clave en claro
        usuario.setTelefono(req.telefono());
        usuario.getRoles().add(rolCliente);

        Usuario guardado = usuarioRepository.save(usuario);
        UserDetails detalles = userDetailsService.toUserDetails(guardado);
        return new AuthResponse(jwtService.generarToken(detalles), "Bearer");
    }

    public AuthResponse login(LoginRequest req) {
        // Spring compara la clave enviada con el hash guardado. Si no coincide, lanza excepción.
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(normalizar(req.email()), req.password()));

        UserDetails detalles = (UserDetails) auth.getPrincipal();
        return new AuthResponse(jwtService.generarToken(detalles), "Bearer");
    }

    private String normalizar(String email) {
        return email.trim().toLowerCase();
    }
}
