package com.proyecto.galeria.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.proyecto.galeria.model.EquipoUsuario;


@Repository
public interface EquipoUsuarioRepository extends JpaRepository<EquipoUsuario, Integer> {
    List<EquipoUsuario> findByUsuarioId(Integer usuarioId);
    List<EquipoUsuario> findByEquipoId(Integer equipoId);
}