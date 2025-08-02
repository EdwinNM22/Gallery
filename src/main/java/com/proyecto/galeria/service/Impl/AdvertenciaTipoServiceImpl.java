package com.proyecto.galeria.service.Impl;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.proyecto.galeria.model.AdvertenciaTipo;
import com.proyecto.galeria.repository.AdvertenciaTipoRepository;
import com.proyecto.galeria.service.AdvertenciaTipoService;

import java.util.List;
import java.util.Optional;

@Service
public class AdvertenciaTipoServiceImpl implements AdvertenciaTipoService {

    @Autowired
    private AdvertenciaTipoRepository advertenciaTipoRepository;

    @Override
    public List<AdvertenciaTipo> findAll() {
        return advertenciaTipoRepository.findAll();
    }

    @Override
    public Optional<AdvertenciaTipo> findById(Integer id) {
        return advertenciaTipoRepository.findById(id);
    }

    @Override
    public AdvertenciaTipo save(AdvertenciaTipo advertenciaTipo) {
        return advertenciaTipoRepository.save(advertenciaTipo);
    }

    @Override
    public AdvertenciaTipo update(Integer id, AdvertenciaTipo advertenciaTipoData) {
        AdvertenciaTipo existingAdvertenciaTipo = advertenciaTipoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("AdvertenciaTipo not found with id: " + id));

        BeanUtils.copyProperties(advertenciaTipoData, existingAdvertenciaTipo, "id");

        return advertenciaTipoRepository.save(existingAdvertenciaTipo);
    }

    @Override
    public void delete(Integer id) {
        AdvertenciaTipo existing = advertenciaTipoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("UsuarioAdvertencia not found with id: " + id));
        advertenciaTipoRepository.delete(existing);
    }

    @Override
    public Optional<AdvertenciaTipo> findByName(String name) {
        return advertenciaTipoRepository.findByName(name);
    }
}
