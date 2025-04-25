package com.proyecto.galeria.service;
import com.proyecto.galeria.model.SubAlbum;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public interface subAlbumService {

    public SubAlbum save(SubAlbum subAlbum);
    public Optional<SubAlbum> get(Integer id);
    public SubAlbum update(SubAlbum subAlbum);
    public void delete(Integer id);
    public List<SubAlbum> findAll();
    public List<SubAlbum> getSubAlbumesAntes();
    public List<SubAlbum> getSubAlbumesDespues();
    public List<SubAlbum> findByAlbumId(Integer albumId);




}




