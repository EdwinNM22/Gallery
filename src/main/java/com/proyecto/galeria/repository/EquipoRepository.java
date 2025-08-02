package com.proyecto.galeria.repository;

import com.proyecto.galeria.model.Equipo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EquipoRepository extends JpaRepository<Equipo, Integer> {
    List<Equipo> findByProyectoId(Integer proyectoId);

    @Query("SELECT e FROM Equipo e JOIN e.miembros m WHERE m.id = :usuarioId")
    List<Equipo> findEquiposByUsuarioId(@Param("usuarioId") Integer usuarioId);

    @Query("SELECT e FROM Equipo e JOIN e.miembros eu WHERE eu.usuarioAdvertencia.id = :uaId")
    Optional<Equipo> findByUsuarioAdvertenciaId(@Param("uaId") Integer uaId);
}