package com.proyecto.galeria.repository;


import com.proyecto.galeria.model.Permiso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PermisoRepository extends JpaRepository<Permiso, Integer> {
    Optional<Permiso> findByCodigo(String codigo);
    List<Permiso> findByGrupoVista(String grupoVista);
    List<Permiso> findByTipoPermiso(String tipoPermiso);
    List<Permiso> findByGrupoVistaAndTipoPermiso(String grupoVista, String tipoPermiso);
}
