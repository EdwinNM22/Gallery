package com.proyecto.galeria.repository;

import com.proyecto.galeria.model.foto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface fotoRepository extends JpaRepository<foto, Integer> {
}
