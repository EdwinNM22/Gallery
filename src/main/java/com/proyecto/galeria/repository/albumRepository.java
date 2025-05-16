package com.proyecto.galeria.repository;

import com.proyecto.galeria.model.album;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface albumRepository extends JpaRepository<album, Integer> {

    @Query("SELECT DISTINCT a FROM album a JOIN a.equipos e JOIN e.miembros m WHERE m.id = :usuarioId")
    List<album> findAllByUsuarioAsignado(@Param("usuarioId") Integer usuarioId);

}
