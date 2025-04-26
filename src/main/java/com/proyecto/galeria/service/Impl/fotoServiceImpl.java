package com.proyecto.galeria.service.Impl;

import com.proyecto.galeria.model.foto;
import com.proyecto.galeria.repository.fotoRepository;
import com.proyecto.galeria.service.fotoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
@Service
public class fotoServiceImpl implements fotoService {

    @Autowired
    private fotoRepository fotoRepository;

    @Override
    public foto save(foto foto) {
        return fotoRepository.save(foto);
    }

    @Override
    public Optional<foto> get(Integer id) {
        return fotoRepository.findById(id);
    }

    @Override
    public void update(foto foto) {
        fotoRepository.save(foto);
    }

    @Override
    public void delete(Integer id) {
        fotoRepository.deleteById(id);

    }

    @Override
    public List<foto> findAll() {
        return fotoRepository.findAll();
    }

    @Override
    public List<foto> saveAll(List<foto> fotos) {
        return fotoRepository.saveAll(fotos);
    }
}
