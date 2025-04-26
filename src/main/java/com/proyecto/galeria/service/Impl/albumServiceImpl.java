package com.proyecto.galeria.service.Impl;

import com.proyecto.galeria.model.album;
import com.proyecto.galeria.repository.albumRepository;
import com.proyecto.galeria.service.albumService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class albumServiceImpl implements albumService {

    private final albumRepository albumRepo;

    @Autowired
    public albumServiceImpl(albumRepository albumRepo) {
        this.albumRepo = albumRepo;
    }

    @Override
    public album save(album album) {
        return albumRepo.save(album);
    }

    @Override
    public Optional<album> get(Integer id) {
        return albumRepo.findById(id);
    }

    @Override
    public void update(album album) {
        albumRepo.save(album);
    }

    @Override
    public void delete(Integer id) {
        albumRepo.deleteById(id);
    }

    @Override
    public List<album> findAll() {
        return albumRepo.findAll();
    }
}
