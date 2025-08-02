package com.proyecto.galeria.service;

import java.util.List;
import java.util.Optional;

import com.proyecto.galeria.model.UsuarioAdvertencia;

public interface UsuarioAdvertenciaService {
    List<UsuarioAdvertencia> findAll();

    Optional<UsuarioAdvertencia> findById(Integer id);

    UsuarioAdvertencia save(UsuarioAdvertencia usuarioAdvertencia);

    UsuarioAdvertencia update(Integer id, UsuarioAdvertencia newUsuarioAdvertencia);

    void delete(Integer id);

    List<UsuarioAdvertencia> findByUsuarioId(Integer id);

    List<UsuarioAdvertencia> findByAdvertenciaTipoId(Integer id);
}