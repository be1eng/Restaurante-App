package com.example.demo.seguridad.repository;

import com.example.demo.seguridad.entity.Usuario;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    boolean existsByEmail(String email);

    // Trae el usuario junto con sus roles, permisos y restaurante en una sola consulta
    @EntityGraph(attributePaths = {"roles", "roles.permisos", "restaurante"})
    Optional<Usuario> findByEmail(String email);
}
