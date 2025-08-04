package com.proyecto.galeria.repository;

import com.proyecto.galeria.controller.ProyectoPlanoController;
import com.proyecto.galeria.model.ProyectoPlano;
import com.proyecto.galeria.model.usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProyectoPlanoRepository extends JpaRepository<ProyectoPlano, Long> {
    List<ProyectoPlano> findByUsuario(usuario usuario);
}
