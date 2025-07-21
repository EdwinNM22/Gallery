package com.proyecto.galeria.repository;


import com.proyecto.galeria.model.Expediente;
import com.proyecto.galeria.model.usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExpedienteRepository extends JpaRepository<Expediente, Integer> {
    List<Expediente> findByUsuario(usuario usuario);

}
