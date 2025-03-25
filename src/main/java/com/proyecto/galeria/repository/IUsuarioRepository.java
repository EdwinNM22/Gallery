package com.proyecto.galeria.repository;

import com.proyecto.galeria.model.usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IUsuarioRepository extends JpaRepository<usuario, Integer> {
    Optional<usuario> findByEmail(String email);
}