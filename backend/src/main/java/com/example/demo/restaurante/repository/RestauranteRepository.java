package com.example.demo.restaurante.repository;

import com.example.demo.restaurante.entity.Restaurante;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RestauranteRepository extends JpaRepository<Restaurante, Long> {
}
