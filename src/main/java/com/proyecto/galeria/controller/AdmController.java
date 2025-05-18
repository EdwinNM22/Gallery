package com.proyecto.galeria.controller;

import com.proyecto.galeria.model.Permiso;
import com.proyecto.galeria.model.album;
import com.proyecto.galeria.service.IUsuarioService;
import com.proyecto.galeria.service.albumService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/adm")
public class AdmController {
    @Autowired
    private albumService albumService;
    @Autowired private IUsuarioService usuarioService;

    @GetMapping("")
    public String home(Model model, HttpSession session) {
        List<album> albumes = albumService.findAll();
        model.addAttribute("albumes", albumes);

        Integer idUsuario = (Integer) session.getAttribute("idusuario");
        usuarioService.findById(idUsuario).ifPresentOrElse(user -> {
            user.getPermisos().size(); // Forzar carga

            model.addAttribute("usuarioLogueado", user);

            // Permisos individuales
            Set<String> permisos = user.getPermisos().stream()
                    .map(Permiso::getCodigo)
                    .collect(Collectors.toSet());

            model.addAttribute("AGENDA_ACCESS", permisos.contains("AGENDA_ACCESS"));
            model.addAttribute("PROYECTOS_ACCESS", permisos.contains("PROYECTOS_ACCESS"));
            model.addAttribute("REPORTES_ACCESS", permisos.contains("REPORTES_ACCESS"));
            model.addAttribute("USUARIOS_ACCESS", permisos.contains("USUARIOS_ACCESS"));
            model.addAttribute("USUARIOS_CREATE", permisos.contains("USUARIOS_CREATE"));

        }, () -> {
            model.addAttribute("AGENDA_ACCESS", false);
            model.addAttribute("PROYECTOS_ACCESS", false);
            model.addAttribute("REPORTES_ACCESS", false);
            model.addAttribute("USUARIOS_ACCESS", false);
            model.addAttribute("USUARIOS_CREATE", false);
        });
        return "adm/home";
    }
}


