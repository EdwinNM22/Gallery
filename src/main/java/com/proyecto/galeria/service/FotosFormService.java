package com.proyecto.galeria.service;

import com.proyecto.galeria.model.FotosForm;

import java.util.List;
import java.util.Optional;

public interface FotosFormService {
    List<FotosForm> findAll();
    Optional<FotosForm> findById(Integer id);
    FotosForm save(FotosForm fotosForm);
    FotosForm update(Integer id, FotosForm fotosForm);
    void delete(Integer id);
    List<FotosForm> findByFormId(Integer formId);
}