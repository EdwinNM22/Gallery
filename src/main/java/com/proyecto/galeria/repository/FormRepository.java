package com.proyecto.galeria.repository;

import com.proyecto.galeria.model.Form;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FormRepository extends JpaRepository<Form, Integer> {
    List<Form> findByUsuario_Id(Integer usuarioId);
    List<Form> findByUsuario_IdAndExpediente_Id(Integer usuarioId, Integer expedienteId);
    List<Form> findByExpediente_Id(Integer expedienteId);

}