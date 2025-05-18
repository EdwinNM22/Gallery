package com.proyecto.galeria.controller;


import com.proyecto.galeria.model.Permiso;
import com.proyecto.galeria.model.usuario;
import com.proyecto.galeria.repository.PermisoRepository;
import com.proyecto.galeria.service.IUsuarioService;
import com.proyecto.galeria.service.PermisoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/permiso")
public class PermisoController {

    @Autowired
    private IUsuarioService usuarioService;

    @Autowired
    private PermisoService permisoService;

    @GetMapping("/management")
    public String showPermissionsManagement(Model model) {
        model.addAttribute("permisosAgrupados", permisoService.getPermisosAgrupadosPorVista());
        return "permiso/permissions-management";
    }

    @GetMapping("/users/{userId}/permissions")
    @ResponseBody
    public List<Permiso> getUserPermissions(@PathVariable Integer userId) {
        Optional<usuario> userOpt = usuarioService.getUsuarioById(userId);
        if (userOpt.isPresent()) {
            // Convertimos el Set<Permiso> a List<Permiso>
            return new ArrayList<>(userOpt.get().getPermisos());
        }
        return Collections.emptyList();
    }

    @PostMapping("/users/{userId}/permissions/update")
    @ResponseBody
    public ResponseEntity<?> updatePermissions(@PathVariable Integer userId,
                                               @RequestBody List<Integer> newPermIds) {
        try {
            permisoService.actualizarPermisosDeUsuario(userId, newPermIds);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al actualizar permisos: " + e.getMessage());
        }
    }
}

