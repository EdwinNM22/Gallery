package com.proyecto.galeria.service.Impl;

import com.proyecto.galeria.model.Permiso;
import com.proyecto.galeria.repository.PermisoRepository;
import com.proyecto.galeria.service.PermisoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PermisoServiceImpl implements PermisoService {

    private final PermisoRepository permisoRepository;

    @Autowired
    public PermisoServiceImpl(PermisoRepository permisoRepository) {
        this.permisoRepository = permisoRepository;
    }

    @Override
    public List<Permiso> getAllPermisos() {
        return permisoRepository.findAll();
    }

    @Override
    public Optional<Permiso> getPermisoById(Integer id) {
        return permisoRepository.findById(id);
    }

    @Override
    public Optional<Permiso> getPermisoByNombre(String nombre) {
        return permisoRepository.findByNombre(nombre);
    }
}
