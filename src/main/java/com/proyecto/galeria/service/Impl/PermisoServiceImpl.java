package com.proyecto.galeria.service.Impl;

import com.proyecto.galeria.model.Permiso;
import com.proyecto.galeria.model.usuario;
import com.proyecto.galeria.repository.PermisoRepository;
import com.proyecto.galeria.service.IUsuarioService;
import com.proyecto.galeria.service.PermisoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PermisoServiceImpl implements PermisoService {

    private final PermisoRepository permisoRepository;

    @Autowired
    private IUsuarioService usuarioService;

    @Autowired
    public PermisoServiceImpl(PermisoRepository permisoRepository) {
        this.permisoRepository = permisoRepository;
    }

    @Override
    public List<Permiso> getAllPermisos() {
        return permisoRepository.findAll();
    }

    @Override
    public Optional<Permiso> getPermisoById(Integer id) {
        return permisoRepository.findById(id);
    }

    @Override
    public Optional<Permiso> getPermisoByCodigo(String codigo) {
        return permisoRepository.findByCodigo(codigo);
    }

    @Override
    public void actualizarPermisosDeUsuario(Integer userId, List<Integer> newPermIds) {
        Optional<usuario> userOpt = usuarioService.getUsuarioById(userId);
        if (userOpt.isPresent()) {
            usuario user = userOpt.get();
            List<Permiso> nuevosPermisos = permisoRepository.findAllById(newPermIds);
            user.setPermisos(new HashSet<>(nuevosPermisos));
            usuarioService.save(user);
        } else {
            throw new RuntimeException("Usuario no encontrado");
        }
    }

    @Override
    public Map<String, List<Permiso>> getPermisosAgrupadosPorVista() {
        List<Permiso> allPermisos = getAllPermisos();
        return allPermisos.stream()
                .collect(Collectors.groupingBy(Permiso::getGrupoVista));
    }

    @Override
    public List<Permiso> getPermisosPorVista(String vista) {
        return permisoRepository.findByGrupoVista(vista);
    }
}
