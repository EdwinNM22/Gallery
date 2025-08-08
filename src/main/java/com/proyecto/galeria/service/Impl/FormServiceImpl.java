package com.proyecto.galeria.service.Impl;

import com.proyecto.galeria.model.Form;
import com.proyecto.galeria.repository.FormRepository;
import com.proyecto.galeria.service.FormService;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
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
    public Form update(Integer id, Form newFormData) { // This is your current BeanUtils method
        Form existingForm = formRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Form not found with id: " + id));

        BeanUtils.copyProperties(newFormData, existingForm, "id");

        return formRepository.save(existingForm);
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
    public List<Form> findByFuturo(Boolean futuro) {
        return formRepository.findByFuturo(futuro);
    }

    @Override
    public List<Form> findByUsuarioIdAndFuturo(Integer usuarioId, Boolean futuro) {
        return formRepository.findByUsuarioIdAndFuturo(usuarioId, futuro);
    }

    @Override
    public List<LocalTime> findAllHoraEvaluacion(LocalDate date) {
        return formRepository.findAllHoraEvaluacionByFecha(date);
    }

}