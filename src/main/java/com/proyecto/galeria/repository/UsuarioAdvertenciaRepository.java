package com.proyecto.galeria.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.proyecto.galeria.model.UsuarioAdvertencia;


@Repository
public interface UsuarioAdvertenciaRepository extends JpaRepository<UsuarioAdvertencia, Integer> {
    List<UsuarioAdvertencia> findByUsuarioId(Integer usuarioId);
    List<UsuarioAdvertencia> findByAdvertenciaTipoId(Integer advertenciaTipoId);
}