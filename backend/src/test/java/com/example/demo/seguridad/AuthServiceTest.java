package com.example.demo.seguridad;

import com.example.demo.comun.excepcion.EmailYaRegistradoException;
import com.example.demo.seguridad.dto.AuthResponse;
import com.example.demo.seguridad.dto.LoginRequest;
import com.example.demo.seguridad.dto.RegistroRequest;
import com.example.demo.seguridad.entity.Rol;
import com.example.demo.seguridad.entity.Usuario;
import com.example.demo.seguridad.jwt.JwtService;
import com.example.demo.seguridad.repository.RolRepository;
import com.example.demo.seguridad.repository.UsuarioRepository;
import com.example.demo.seguridad.service.AuthService;
import com.example.demo.seguridad.service.CustomUserDetailsService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Prueba UNITARIA: no usa Spring ni base de datos, todo lo externo está "simulado" (mock). */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock UsuarioRepository usuarioRepository;
    @Mock RolRepository rolRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock AuthenticationManager authenticationManager;
    @Mock CustomUserDetailsService userDetailsService;
    @Mock JwtService jwtService;
    @InjectMocks AuthService authService;

    private UserDetails detalles(String email) {
        return User.withUsername(email).password("x").authorities("ROLE_CLIENTE").build();
    }

    @Test
    void registrar_guardaUsuarioConPasswordHasheadaYRolCliente() {
        Rol cliente = new Rol();
        cliente.setNombre("CLIENTE");
        UserDetails ud = detalles("ana@mail.com");

        when(usuarioRepository.existsByEmail("ana@mail.com")).thenReturn(false);
        when(rolRepository.findByNombre("CLIENTE")).thenReturn(Optional.of(cliente));
        when(passwordEncoder.encode("Clave1234")).thenReturn("HASH");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(i -> i.getArgument(0));
        when(userDetailsService.toUserDetails(any(Usuario.class))).thenReturn(ud);
        when(jwtService.generarToken(ud)).thenReturn("token-falso");

        AuthResponse r = authService.registrar(
                new RegistroRequest("Ana", "  ANA@mail.com ", "Clave1234", null));

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());
        Usuario guardado = captor.getValue();

        assertEquals("ana@mail.com", guardado.getEmail());      // se normalizó
        assertEquals("HASH", guardado.getPasswordHash());       // se guardó el hash
        assertTrue(guardado.getRoles().contains(cliente));      // rol por defecto
        assertEquals("token-falso", r.token());
        assertEquals("Bearer", r.tipo());
    }

    @Test
    void registrar_conEmailDuplicado_lanzaExcepcionYNoGuarda() {
        when(usuarioRepository.existsByEmail("ana@mail.com")).thenReturn(true);

        assertThrows(EmailYaRegistradoException.class, () ->
                authService.registrar(new RegistroRequest("Ana", "ana@mail.com", "Clave1234", null)));

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void login_correcto_devuelveToken() {
        UserDetails ud = detalles("ana@mail.com");
        when(authenticationManager.authenticate(any()))
                .thenReturn(new UsernamePasswordAuthenticationToken(ud, null, ud.getAuthorities()));
        when(jwtService.generarToken(ud)).thenReturn("token-falso");

        AuthResponse r = authService.login(new LoginRequest("ana@mail.com", "Clave1234"));

        assertEquals("token-falso", r.token());
    }

    @Test
    void login_conCredencialesIncorrectas_propagaBadCredentials() {
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("mal"));

        assertThrows(BadCredentialsException.class, () ->
                authService.login(new LoginRequest("ana@mail.com", "otra")));
    }
}
