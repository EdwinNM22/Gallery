package com.proyecto.galeria.service.Impl;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.proyecto.galeria.model.UsuarioAdvertencia;
import com.proyecto.galeria.repository.UsuarioAdvertenciaRepository;
import com.proyecto.galeria.service.UsuarioAdvertenciaService;

import java.util.List;
import java.util.Optional;

@Service
public class UsuarioAdvertenciaServiceImpl implements UsuarioAdvertenciaService {

    @Autowired
    private UsuarioAdvertenciaRepository usuarioAdvertenciaRepository;

    @Override
    public List<UsuarioAdvertencia> findAll() {
        return usuarioAdvertenciaRepository.findAll();
    }

    @Override
    public Optional<UsuarioAdvertencia> findById(Integer id) {
        return usuarioAdvertenciaRepository.findById(id);
    }

    @Override
    public UsuarioAdvertencia save(UsuarioAdvertencia usuarioAdvertencia) {
        return usuarioAdvertenciaRepository.save(usuarioAdvertencia);
    }

    @Override
    public UsuarioAdvertencia update(Integer id, UsuarioAdvertencia usuarioAdvertenciaData) {
        UsuarioAdvertencia existingUsuarioAdvertencia = usuarioAdvertenciaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("UsuarioAdvertencia not found with id: " + id));

        BeanUtils.copyProperties(usuarioAdvertenciaData, existingUsuarioAdvertencia, "id");

        return usuarioAdvertenciaRepository.save(existingUsuarioAdvertencia);
    }

    @Override
    public void delete(Integer id) {
        UsuarioAdvertencia existing = usuarioAdvertenciaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("UsuarioAdvertencia not found with id: " + id));
        usuarioAdvertenciaRepository.delete(existing);
    }

    @Override
    public List<UsuarioAdvertencia> findByUsuarioId(Integer id) {
        return usuarioAdvertenciaRepository.findByUsuarioId(id);
    }

    @Override
    public List<UsuarioAdvertencia> findByAdvertenciaTipoId(Integer id) {
        return usuarioAdvertenciaRepository.findByAdvertenciaTipoId(id);
    }
}
