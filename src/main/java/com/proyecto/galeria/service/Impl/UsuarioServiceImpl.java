package com.proyecto.galeria.service.Impl;

import com.proyecto.galeria.model.usuario;
import com.proyecto.galeria.repository.IUsuarioRepository;
import com.proyecto.galeria.service.IUsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UsuarioServiceImpl implements IUsuarioService {

    @Autowired
    private IUsuarioRepository usuarioRepository;

    @Override
    public Optional<usuario> findById(Integer id) {
        return usuarioRepository.findById(id);
    }

    @Override
    public usuario save(usuario usuario) {
        return usuarioRepository.save(usuario);
    }

    @Override
    public Optional<usuario> findByEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }

    @Override
    public Optional<usuario> get(Integer id) {
        return usuarioRepository.findById(id);
    }

    @Override
    public void update(usuario usuario) {
        usuarioRepository.save(usuario);

    }

    @Override
    public void delete(Integer id) {
        usuarioRepository.deleteById(id);

    }

    @Override
    public List<usuario> findAll() {
        return usuarioRepository.findAll();
    }

}