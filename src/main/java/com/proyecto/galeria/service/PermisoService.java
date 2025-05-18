package com.proyecto.galeria.service;

import com.proyecto.galeria.model.Permiso;
import com.proyecto.galeria.model.usuario;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface PermisoService {
    List<Permiso> getAllPermisos();
    Optional<Permiso> getPermisoById(Integer id);
    Optional<Permiso> getPermisoByCodigo(String codigo);
    void actualizarPermisosDeUsuario(Integer userId, List<Integer> newPermIds);
    Map<String, List<Permiso>> getPermisosAgrupadosPorVista();
    List<Permiso> getPermisosPorVista(String vista);
}

