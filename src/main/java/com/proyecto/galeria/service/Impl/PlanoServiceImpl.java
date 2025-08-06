package com.proyecto.galeria.service.Impl;

import com.proyecto.galeria.model.Plano;
import com.proyecto.galeria.model.usuario;
import com.proyecto.galeria.repository.PlanoRepository;
import com.proyecto.galeria.service.PlanoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PlanoServiceImpl implements PlanoService {

    @Autowired
    private PlanoRepository planoRepository;

    @Override
    public Plano guardarPlanoConMediciones(Plano plano) {
        return planoRepository.save(plano);
    }

    @Override
    public List<Plano> findByUsuario(usuario usuario) {
        return planoRepository.findByUsuario(usuario);
    }

    @Override
    public List<Plano> findByProyectoPlano(Long proyectoId) {
        return planoRepository.findByProyectoPlanoId(proyectoId);
    }

    @Override
    public Optional<Plano> findById(Long id) {
        return planoRepository.findById(id);
    }


    @Override
    public void eliminar(Plano plano) {
        planoRepository.delete(plano);
    }
}
