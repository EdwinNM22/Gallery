package com.proyecto.galeria.service.Impl;

import com.proyecto.galeria.model.Form;
import com.proyecto.galeria.repository.FormRepository;
import com.proyecto.galeria.service.FormService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class FormServiceImpl implements FormService {

    @Autowired
    private FormRepository formRepository;

    @Override
    public List<Form> findAll() {
        return formRepository.findAll();
    }

    @Override
    public Optional<Form> findById(Integer id) {
        return formRepository.findById(id);
    }

    @Override
    public Form save(Form form) {
        return formRepository.save(form);
    }

    @Override
    public Form update(Integer id, Form newForm) {
        Form form = formRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Form not found with id: " + id));
        // Fix: set the ID on newForm and return the saved form
        newForm.setId(id);
        return formRepository.save(newForm);
    }

    @Override
    public void delete(Integer id) {
        Form existing = formRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Form not found with id: " + id));
        formRepository.deleteById(existing.getId());
    }

    // Add this method implementation
    @Override
    public boolean existsById(Integer id) {
        return formRepository.existsById(id);
    }

    @Override
    public List<Form> findByUsuarioId(Integer usuarioId) {
        return formRepository.findByUsuario_Id(usuarioId);
    }

    @Override
    public List<Form> findByUsuarioIdAndExpedienteIdAndFuturo(Integer usuarioId, Integer expedienteId, Boolean futuro) {
        return formRepository.findByUsuario_IdAndExpediente_IdAndFuturo(usuarioId, expedienteId, futuro);
    }

    @Override
    public List<Form> findByExpedienteIdAndFuturo(Integer expedienteId, Boolean futuro) {
        return formRepository.findByExpediente_IdAndFuturo(expedienteId, futuro);
    }


}