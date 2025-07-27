package com.proyecto.galeria.service;

import com.proyecto.galeria.model.usuario;

import java.util.List;
import java.util.Optional;

public interface IUsuarioService {

    List<usuario> findAll();
    Optional<usuario> findById(Integer id);
    usuario save (usuario usuario);
    Optional<usuario> findByEmail(String email);
    Optional<usuario> findByUsername(String username);
    Optional<usuario> findBynombre(String nombre);
    public Optional<usuario> get(Integer id);
    public void update(usuario usuario);
    public void delete(Integer id);

    //Agreado para los permisos
    Optional<usuario> getUsuarioById(Integer id);
    void asignarPermisoAUsuario(Integer usuarioId, Integer permisoId);
    void quitarPermisoDeUsuario(Integer usuarioId, Integer permisoId);

}