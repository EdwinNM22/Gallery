package com.proyecto.galeria.service;

import com.proyecto.galeria.model.FotosForm;

import java.util.List;
import java.util.Optional;

public interface FotosFormService {
    List<FotosForm> findAll();

    List<FotosForm> findAllById(List<Integer> ids);

    Optional<FotosForm> findById(Integer id);

    FotosForm save(FotosForm fotosForm);

    FotosForm update(Integer id, FotosForm fotosForm);

    void delete(Integer id);

    List<FotosForm> findByFormId(Integer formId);

    void deleteById(Integer id);

    void deleteByFormId(Integer formId);

    void deleteByIds(List<Integer> ids);
}