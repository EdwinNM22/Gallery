package com.proyecto.galeria.service.Impl;

import com.proyecto.galeria.model.Permiso;
import com.proyecto.galeria.model.usuario;
import com.proyecto.galeria.repository.IUsuarioRepository;
import com.proyecto.galeria.repository.PermisoRepository;
import com.proyecto.galeria.service.IUsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UsuarioServiceImpl implements IUsuarioService {

    @Autowired
    private IUsuarioRepository usuarioRepository;
    @Autowired
    private PermisoRepository permisoRepository;

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


    // Nuevos para los permisos

    @Override
    public void asignarPermisoAUsuario(Integer usuarioId, Integer permisoId) {
        Optional<usuario> usuarioOpt = usuarioRepository.findById(usuarioId);
        Optional<Permiso> permisoOpt = permisoRepository.findById(permisoId);

        if (usuarioOpt.isPresent() && permisoOpt.isPresent()) {
            usuario usuario = usuarioOpt.get();
            Permiso permiso = permisoOpt.get();

            usuario.getPermisos().add(permiso);
            usuarioRepository.save(usuario);
        } else {
            throw new RuntimeException("Usuario o permiso no encontrado");
        }
    }

    @Override
    public void quitarPermisoDeUsuario(Integer usuarioId, Integer permisoId) {
        Optional<usuario> usuarioOpt = usuarioRepository.findById(usuarioId);
        Optional<Permiso> permisoOpt = permisoRepository.findById(permisoId);

        if (usuarioOpt.isPresent() && permisoOpt.isPresent()) {
            usuario usuario = usuarioOpt.get();
            Permiso permiso = permisoOpt.get();

            usuario.getPermisos().remove(permiso);
            usuarioRepository.save(usuario);
        } else {
            throw new RuntimeException("Usuario o permiso no encontrado");
        }
    }

    @Override
    public Optional<usuario> getUsuarioById(Integer id) {
        return usuarioRepository.findById(id);
    }


}