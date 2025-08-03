package com.proyecto.galeria.repository;

import com.proyecto.galeria.model.Plano;
import com.proyecto.galeria.model.usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlanoRepository extends JpaRepository<Plano, Long> {
    List<Plano> findByUsuario(usuario usuario);
}
