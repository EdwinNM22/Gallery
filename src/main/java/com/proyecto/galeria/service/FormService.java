package com.proyecto.galeria.service;

import com.proyecto.galeria.model.Form;

import java.util.List;
import java.util.Optional;

public interface FormService {
    List<Form> findAll();
    Optional<Form> findById(Integer id);
    Form save(Form form);
    Form update(Integer id, Form newForm);
    void delete(Integer id);
    List<Form> findByUsuarioId(Integer usuarioId);
    List<Form> findByUsuarioIdAndExpedienteId(Integer usuarioId, Integer expedienteId);

    List<Form> findByExpedienteId(Integer expedienteId);
}