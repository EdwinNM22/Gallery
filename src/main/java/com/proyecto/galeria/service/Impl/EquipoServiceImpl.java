package com.proyecto.galeria.service.Impl;

import com.proyecto.galeria.model.Equipo;
import com.proyecto.galeria.repository.EquipoRepository;
import com.proyecto.galeria.service.EquipoService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EquipoServiceImpl implements EquipoService {

    private final EquipoRepository equipoRepository;

    public EquipoServiceImpl(EquipoRepository equipoRepository) {
        this.equipoRepository = equipoRepository;
    }

    @Override
    public List<Equipo> findAll() {
        return equipoRepository.findAll();
    }

    @Override
    public List<Equipo> findByProyectoId(Integer proyectoId) {
        return equipoRepository.findByProyectoId(proyectoId);
    }

    @Override
    public Equipo save(Equipo equipo) {
        return equipoRepository.save(equipo);
    }

    @Override
    public Optional<Equipo> findById(Integer id) {
        return equipoRepository.findById(id);
    }

    @Override
    public void delete(Integer id) {
        equipoRepository.deleteById(id);
    }

    @Override
    public boolean existsById(Integer id) {
        return equipoRepository.existsById(id);
    }
}