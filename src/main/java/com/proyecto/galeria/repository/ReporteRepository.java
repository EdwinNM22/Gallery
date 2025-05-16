package com.proyecto.galeria.repository;

import com.proyecto.galeria.model.album;
import com.proyecto.galeria.model.reporte;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface ReporteRepository extends JpaRepository<reporte, Integer> {

    List<reporte> findByAlbumOrderByFechaHoraDesc(album album);
    List<reporte> findByAlbumAndEstadoOrderByFechaHoraDesc(album album, reporte.Estado estado);


    List<reporte> findByEstadoOrderByFechaHoraDesc(reporte.Estado estado);

    boolean existsByAlbumAndAutorIdAndEstadoAndFechaHoraBetween(
            album album, Integer autorId,
            reporte.Estado estado,
            LocalDateTime start, LocalDateTime end);
}
