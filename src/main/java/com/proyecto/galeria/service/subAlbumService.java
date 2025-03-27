package com.proyecto.galeria.service;
import com.proyecto.galeria.model.SubAlbum;
import java.util.List;
import java.util.Optional;

public interface subAlbumService {

    public SubAlbum save(SubAlbum subAlbum);
    public Optional<SubAlbum> get(Integer id);
    public SubAlbum update(SubAlbum subAlbum);
    public void delete(Integer id);
    public List<SubAlbum> findAll();
    public List<SubAlbum> getSubAlbumesAntes();
    public List<SubAlbum> getSubAlbumesDespues();




}




