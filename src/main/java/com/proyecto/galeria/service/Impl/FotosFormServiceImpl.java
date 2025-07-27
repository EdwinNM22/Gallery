package com.proyecto.galeria.service.Impl;

import com.proyecto.galeria.model.FotosForm;
import com.proyecto.galeria.repository.FotosFormRepository;
import com.proyecto.galeria.service.FotosFormService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class FotosFormServiceImpl implements FotosFormService {

    @Autowired
    private FotosFormRepository fotosFormRepository;

    @Override
    public List<FotosForm> findAll() {
        return fotosFormRepository.findAll();
    }

    @Override
    public Optional<FotosForm> findById(Integer id) {
        return fotosFormRepository.findById(id);
    }

    @Override
    public FotosForm save(FotosForm fotosForm) {
        return fotosFormRepository.save(fotosForm);
    }

    @Override
    public FotosForm update(Integer id, FotosForm fotosForm) {
        FotosForm existing = fotosFormRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("FotosForm not found with id: " + id));
        fotosForm.setId(existing.getId());
        return fotosFormRepository.save(fotosForm);
    }

    @Override
    public void delete(Integer id) {
        FotosForm existing = fotosFormRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("FotosForm not found with id: " + id));
        fotosFormRepository.deleteById(existing.getId());
    }

    @Override
    public List<FotosForm> findByFormId(Integer formId) {
        return fotosFormRepository.findByForm_Id(formId);
    }

    @Override
    public void deleteById(Integer id) {
        fotosFormRepository.deleteById(id);
    }
    
    @Override
    public void deleteByFormId(Integer formId) {
        List<FotosForm> fotos = fotosFormRepository.findByForm_Id(formId);
        fotosFormRepository.deleteAll(fotos);
    }
}