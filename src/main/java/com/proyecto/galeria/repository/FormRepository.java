package com.proyecto.galeria.repository;

import com.proyecto.galeria.model.Form;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface FormRepository extends JpaRepository<Form, Integer> {
    List<Form> findByFuturo(Boolean futuro);

    List<Form> findByUsuario_Id(Integer usuarioId);

    @Query("SELECT f FROM Form f WHERE f.usuario.id = :usuarioId AND f.futuro = :futuro")
    List<Form> findByUsuarioIdAndFuturo(@Param("usuarioId") Integer usuarioId, @Param("futuro") Boolean futuro);

    @Query("SELECT f.horaEvaluacion FROM Form f WHERE f.fechaEvaluacion = :fecha")
    List<LocalTime> findAllHoraEvaluacionByFecha(@Param("fecha") LocalDate fecha);
}