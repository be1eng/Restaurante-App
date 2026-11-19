package com.example.demo.seguridad.service;

import com.example.demo.comun.excepcion.EmailYaRegistradoException;
import com.example.demo.restaurante.dto.RestauranteRequest;
import com.example.demo.restaurante.entity.Restaurante;
import com.example.demo.restaurante.repository.RestauranteRepository;
import com.example.demo.seguridad.dto.AuthResponse;
import com.example.demo.seguridad.dto.LoginRequest;
import com.example.demo.seguridad.dto.RegistroRequest;
import com.example.demo.seguridad.dto.RegistroRestauranteRequest;
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

    private static final String ROL_CLIENTE = "CLIENTE";
    private static final String ROL_RESTAURANTE = "RESTAURANTE";

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final RestauranteRepository restauranteRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;
    private final JwtService jwtService;

    /** Registro público de un cliente. */
    @Transactional
    public AuthResponse registrar(RegistroRequest req) {
        String email = normalizar(req.email());
        validarEmailLibre(email);

        Usuario usuario = nuevoUsuario(req, email, buscarRol(ROL_CLIENTE));
        return respuestaPara(usuarioRepository.save(usuario));
    }

    /**
     * Registra al dueño y su restaurante en UNA sola transacción:
     * si algo falla (ej. email repetido) no queda ni el restaurante ni el usuario.
     */
    @Transactional
    public AuthResponse registrarRestaurante(RegistroRestauranteRequest req) {
        String email = normalizar(req.propietario().email());
        validarEmailLibre(email);

        Rol rolRestaurante = buscarRol(ROL_RESTAURANTE);
        Restaurante restaurante = restauranteRepository.save(nuevoRestaurante(req.restaurante()));

        Usuario dueno = nuevoUsuario(req.propietario(), email, rolRestaurante);
        dueno.setRestaurante(restaurante);
        return respuestaPara(usuarioRepository.save(dueno));
    }

    public AuthResponse login(LoginRequest req) {
        // Spring compara la clave enviada con el hash guardado. Si no coincide, lanza excepción.
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(normalizar(req.email()), req.password()));

        UserDetails detalles = (UserDetails) auth.getPrincipal();
        return new AuthResponse(jwtService.generarToken(detalles), "Bearer");
    }

    // ---------- Internos ----------

    private void validarEmailLibre(String emailNormalizado) {
        if (usuarioRepository.existsByEmail(emailNormalizado)) {
            throw new EmailYaRegistradoException();
        }
    }

    private Rol buscarRol(String nombre) {
        return rolRepository.findByNombre(nombre)
                .orElseThrow(() -> new IllegalStateException(
                        "No existe el rol " + nombre + ". Revisa que las migraciones V1 y V2 se hayan aplicado."));
    }

    private Usuario nuevoUsuario(RegistroRequest req, String email, Rol rol) {
        Usuario usuario = new Usuario();
        usuario.setNombre(req.nombre().trim());
        usuario.setEmail(email);
        usuario.setPasswordHash(passwordEncoder.encode(req.password()));  // NUNCA se guarda la clave en claro
        usuario.setTelefono(req.telefono());
        usuario.getRoles().add(rol);
        return usuario;
    }

    private Restaurante nuevoRestaurante(RestauranteRequest datos) {
        Restaurante restaurante = new Restaurante();
        restaurante.setNombre(datos.nombre().trim());
        restaurante.setDireccion(datos.direccion().trim());
        restaurante.setTelefono(datos.telefono());
        return restaurante;
    }

    private AuthResponse respuestaPara(Usuario guardado) {
        UserDetails detalles = userDetailsService.toUserDetails(guardado);
        return new AuthResponse(jwtService.generarToken(detalles), "Bearer");
    }

    private String normalizar(String email) {
        return email.trim().toLowerCase();
    }
}
