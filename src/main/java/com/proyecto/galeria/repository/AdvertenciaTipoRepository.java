package com.proyecto.galeria.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.proyecto.galeria.model.AdvertenciaTipo;


@Repository
public interface AdvertenciaTipoRepository extends JpaRepository<AdvertenciaTipo, Integer> {
    Optional<AdvertenciaTipo> findByName(String name);
}