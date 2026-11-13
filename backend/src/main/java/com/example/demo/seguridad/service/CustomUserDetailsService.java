package com.example.demo.seguridad.service;

import com.example.demo.seguridad.entity.Permiso;
import com.example.demo.seguridad.entity.Rol;
import com.example.demo.seguridad.entity.Usuario;
import com.example.demo.seguridad.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

/** Le enseña a Spring Security cómo buscar un usuario en NUESTRA base de datos. */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));
        return toUserDetails(usuario);
    }

    /** Convierte nuestro Usuario al formato que entiende Spring Security. */
    public UserDetails toUserDetails(Usuario usuario) {
        Set<GrantedAuthority> autoridades = new HashSet<>();
        for (Rol rol : usuario.getRoles()) {
            autoridades.add(new SimpleGrantedAuthority("ROLE_" + rol.getNombre()));
            for (Permiso permiso : rol.getPermisos()) {
                autoridades.add(new SimpleGrantedAuthority(permiso.getCodigo()));
            }
        }
        return User.builder()
                .username(usuario.getEmail())
                .password(usuario.getPasswordHash())
                .authorities(autoridades)
                .disabled(!usuario.isActivo())
                .build();
    }
}
