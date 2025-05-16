package com.proyecto.galeria.service;
import com.proyecto.galeria.model.album;
import java.util.List;
import java.util.Optional;

public interface albumService {
    album save(album album);
    Optional<album> get(Integer id);
    void update(album album);
    void delete(Integer id);
    List<album> findAll();
    album findById(Integer id);

    // NUEVO
    List<album> findAllByUsuarioAsignado(Integer usuarioId);
}
