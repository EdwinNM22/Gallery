package com.proyecto.galeria.repository;

import com.proyecto.galeria.model.album;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface albumRepository extends JpaRepository<album, Integer> {
}
