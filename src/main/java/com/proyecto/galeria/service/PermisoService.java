package com.proyecto.galeria.service;

import com.proyecto.galeria.model.Permiso;

import java.util.List;
import java.util.Optional;

public interface PermisoService {
    List<Permiso> getAllPermisos();
    Optional<Permiso> getPermisoById(Integer id);
    Optional<Permiso> getPermisoByNombre(String nombre);
}

