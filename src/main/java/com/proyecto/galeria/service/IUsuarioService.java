package com.proyecto.galeria.service;

import com.proyecto.galeria.model.usuario;

import java.util.List;
import java.util.Optional;

public interface IUsuarioService {

    List<usuario> findAll();
    Optional<usuario> findById(Integer id);
    usuario save (usuario usuario);
    Optional<usuario> findByEmail(String email);
    public Optional<usuario> get(Integer id);
    public void update(usuario usuario);
    public void delete(Integer id);

}