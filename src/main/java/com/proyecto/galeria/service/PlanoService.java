package com.proyecto.galeria.service;

import com.proyecto.galeria.model.Plano;
import com.proyecto.galeria.model.usuario;

import java.util.List;

public interface PlanoService {
    Plano guardarPlanoConMediciones(Plano plano);
    List<Plano> findByUsuario(usuario usuario);
    List<Plano> findByProyectoPlano(Long proyectoId);

}
