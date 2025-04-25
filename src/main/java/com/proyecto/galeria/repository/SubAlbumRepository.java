package com.proyecto.galeria.repository;

import com.proyecto.galeria.model.SubAlbum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubAlbumRepository extends JpaRepository<SubAlbum, Integer> {

    List<SubAlbum> findByTipo(String tipo);
    List<SubAlbum> findByAlbumId(Integer albumId);

}
