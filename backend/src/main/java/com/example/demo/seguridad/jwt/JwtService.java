package com.example.demo.seguridad.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

/** Crea y valida los tokens JWT. Lee la clave y la duración desde application.yml. */
@Service
public class JwtService {

    private final SecretKey clave;
    private final long expiracionMs;

    public JwtService(@Value("${app.jwt.secret}") String secret,
                      @Value("${app.jwt.expiracion-minutos}") long minutos) {
        byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            throw new IllegalStateException("app.jwt.secret debe tener al menos 32 caracteres");
        }
        this.clave = Keys.hmacShaKeyFor(bytes);
        this.expiracionMs = minutos * 60_000L;
    }

    public String generarToken(UserDetails usuario) {
        Date ahora = new Date();
        List<String> roles = usuario.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(a -> a.startsWith("ROLE_"))
                .map(a -> a.substring(5))
                .toList();

        return Jwts.builder()
                .subject(usuario.getUsername())          // el email
                .claim("roles", roles)                    // informativo; los permisos reales se leen de la BD
                .issuedAt(ahora)
                .expiration(new Date(ahora.getTime() + expiracionMs))
                .signWith(clave)                          // la firma que impide falsificarlo
                .compact();
    }

    public String extraerEmail(String token) {
        return leer(token).getSubject();
    }

    public boolean esValido(String token, UserDetails usuario) {
        try {
            Claims c = leer(token);
            return c.getSubject().equals(usuario.getUsername())
                    && c.getExpiration().after(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    // Verifica la firma; lanza JwtException si el token fue alterado o venció
    private Claims leer(String token) {
        return Jwts.parser().verifyWith(clave).build()
                .parseSignedClaims(token).getPayload();
    }
}
