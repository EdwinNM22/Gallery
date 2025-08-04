package com.proyecto.galeria.service;

import com.proyecto.galeria.controller.ProyectoPlanoController;
import com.proyecto.galeria.model.ProyectoPlano;
import com.proyecto.galeria.model.usuario;

import java.util.List;

public interface ProyectoPlanoService {
    List<ProyectoPlano> findAll();
    List<ProyectoPlano> findByUsuario(usuario usuario);
    ProyectoPlano findById(Long id);
    void save(ProyectoPlano proyecto);
    void deleteById(Long id);
}