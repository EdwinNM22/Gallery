package com.proyecto.galeria.service.Impl;


import com.proyecto.galeria.model.ProyectoPlano;
import com.proyecto.galeria.model.usuario;
import com.proyecto.galeria.repository.ProyectoPlanoRepository;
import com.proyecto.galeria.service.ProyectoPlanoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProyectoPlanoServiceImpl implements ProyectoPlanoService {

    @Autowired
    private ProyectoPlanoRepository proyectoPlanoRepository;

    @Override
    public List<ProyectoPlano> findAll() {
        return proyectoPlanoRepository.findAll();
    }

    @Override
    public List<ProyectoPlano> findByUsuario(usuario usuario) {
        return proyectoPlanoRepository.findByUsuario(usuario);
    }

    @Override
    public ProyectoPlano findById(Long id) {
        Optional<ProyectoPlano> optional = proyectoPlanoRepository.findById(id);
        return optional.orElse(null);
    }

    @Override
    public void save(ProyectoPlano proyecto) {
        proyectoPlanoRepository.save(proyecto);
    }

    @Override
    public void deleteById(Long id) {
        proyectoPlanoRepository.deleteById(id);
    }
}
