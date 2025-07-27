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
    
    // Add this method to the interface
    boolean existsById(Integer id);
    List<Form> findByUsuarioId(Integer usuarioId);
    List<Form> findByUsuarioIdAndExpedienteIdAndFuturo(Integer usuarioId, Integer expedienteId, Boolean futuro);

    List<Form> findByExpedienteIdAndFuturo(Integer expedienteId, Boolean futuro);
}