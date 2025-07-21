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
        return formRepository.save(newForm);
    }

    @Override
    public void delete(Integer id) {
        Form existing = formRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Form not found with id: " + id));
        formRepository.deleteById(existing.getId());
    }

    @Override
    public List<Form> findByUsuarioId(Integer usuarioId) {
        return formRepository.findByUsuario_Id(usuarioId);
    }

    @Override
    public List<Form> findByUsuarioIdAndExpedienteId(Integer usuarioId, Integer expedienteId) {
        return formRepository.findByUsuario_IdAndExpediente_Id(usuarioId, expedienteId);
    }

    @Override
    public List<Form> findByExpedienteId(Integer expedienteId) {
        return formRepository.findByExpediente_Id(expedienteId);
    }


}