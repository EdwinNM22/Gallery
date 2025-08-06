package com.proyecto.galeria.service;

import com.proyecto.galeria.model.Plano;
import com.proyecto.galeria.model.usuario;

import java.util.List;
import java.util.Optional;

public interface PlanoService {
    Plano guardarPlanoConMediciones(Plano plano);
    List<Plano> findByUsuario(usuario usuario);
    List<Plano> findByProyectoPlano(Long proyectoId);
    Optional<Plano> findById(Long id);
    void eliminar(Plano plano);
}
