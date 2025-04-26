package com.proyecto.galeria.service.Impl;

import com.proyecto.galeria.model.SubAlbum;
import com.proyecto.galeria.repository.SubAlbumRepository;
import com.proyecto.galeria.service.subAlbumService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SubAlbumServiceImpl implements subAlbumService {



    private final SubAlbumRepository subAlbumRepo;

    @Autowired
    public SubAlbumServiceImpl(SubAlbumRepository subAlbumRepo) {
        this.subAlbumRepo = subAlbumRepo;
    }

    @Override
    public SubAlbum save(SubAlbum subAlbum) {
        return subAlbumRepo.save(subAlbum);
    }

    @Override
    public Optional<SubAlbum> get(Integer id) {
        return subAlbumRepo.findById(id);
    }

    @Override
    public SubAlbum update(SubAlbum subAlbum) {
       return subAlbumRepo.save(subAlbum);
    }

    @Override
    public void delete(Integer id) {
        subAlbumRepo.deleteById(id);

    }

    @Override
    public List<SubAlbum> findAll() {
        return subAlbumRepo.findAll();
    }

    @Override
    public List<SubAlbum> getSubAlbumesAntes() {
        return subAlbumRepo.findByTipo("antes");
    }

    @Override
    public List<SubAlbum> getSubAlbumesDespues() {
        return subAlbumRepo.findByTipo("despues");
    }

    @Override
    public List<SubAlbum> findByAlbumId(Integer albumId) {
        return subAlbumRepo.findByAlbumId(albumId);
    }


}
