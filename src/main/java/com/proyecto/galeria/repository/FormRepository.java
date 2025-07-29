package com.proyecto.galeria.repository;

import com.proyecto.galeria.model.Form;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FormRepository extends JpaRepository<Form, Integer> {
    List<Form> findByFuturo(Boolean futuro);

    List<Form> findByUsuario_Id(Integer usuarioId);

    List<Form> findByUsuario_IdAndExpediente_IdAndFuturo(Integer usuarioId, Integer expedienteId, Boolean futuro);

    List<Form> findByExpediente_IdAndFuturo(Integer expedienteId, Boolean futuro);
}