package com.example.demo.seguridad.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Una acción concreta que se puede permitir, ej: RESTAURANTE_LEER. Tabla: permiso */
@Entity
@Table(name = "permiso")
@Getter @Setter @NoArgsConstructor
public class Permiso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "permiso_id")
    private Long id;

    @Column(nullable = false, length = 100, unique = true)
    private String codigo;

    @Column(length = 255)
    private String descripcion;
}
