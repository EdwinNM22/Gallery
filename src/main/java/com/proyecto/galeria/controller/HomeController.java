package com.proyecto.galeria.controller;

import com.proyecto.galeria.model.Permiso;
import com.proyecto.galeria.model.album;
import com.proyecto.galeria.model.usuario;
import com.proyecto.galeria.service.IUsuarioService;
import com.proyecto.galeria.service.PermisoService;
import com.proyecto.galeria.service.albumService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/")
public class HomeController {

    private final Logger log = LoggerFactory.getLogger(HomeController.class);
    @Autowired
    private albumService albumService;
    @Autowired
    private IUsuarioService usuarioService;

    @Autowired
    private PermisoService permisoService;

    @GetMapping({"/", "/mainMenu"})
    public String mainMenu() {
        return "home/mainMenu";
    }

    @GetMapping("/home")
    public String home(Model model, HttpSession session) {
        Integer userId = Integer.parseInt(session.getAttribute("idusuario").toString());
        Optional<usuario> optionalUsuario = usuarioService.findById(userId);

        // para dar permisos a edgar

        // Buscar el usuario y obtener su rol

        String userRole = optionalUsuario.map(usuario::getTipo_usuario).orElse("USUARIO");
        model.addAttribute("userRole", userRole);


        String tipoUsuario = optionalUsuario.map(usuario::getTipo_usuario).orElse("UNKNOWN");
        boolean isAdmin = "ADMIN".equals(tipoUsuario);



        model.addAttribute("isAdmin", isAdmin);
        model.addAttribute("tipoUsuario", tipoUsuario);

        List<album> albumes = albumService.findAll();
        model.addAttribute("albumes", albumes);
        model.addAttribute("sesion", session.getAttribute("idusuario"));

        log.info("Sesion del usuario: {} ", session.getAttribute("idusuario"));


        //Validar acceso a la vista
        Integer idUsuario = (Integer) session.getAttribute("idusuario");
        Optional<usuario> userOpt = usuarioService.findById(idUsuario);

        if (userOpt.isEmpty() || userOpt.get().getPermisos().stream()
                .noneMatch(p -> "PROYECTOS_ACCESS".equals(p.getCodigo()))) {
            return "redirect:/NoAccess/Access";
        }

        usuarioService.findById(idUsuario).ifPresentOrElse(user -> {
            user.getPermisos().size(); // Forzar carga

            model.addAttribute("usuarioLogueado", user);
            model.addAttribute("usuarios", usuarioService.findAll());
            model.addAttribute("permisosAgrupados", permisoService.getPermisosAgrupadosPorVista());


            // Permisos individuales
            Set<String> permisos = user.getPermisos().stream()
                    .map(Permiso::getCodigo)
                    .collect(Collectors.toSet());

            model.addAttribute("AGENDA_ACCESS", permisos.contains("AGENDA_ACCESS"));
            model.addAttribute("AGENDA_CREATE", permisos.contains("AGENDA_CREATE"));
            model.addAttribute("PROYECTOS_DELETE", permisos.contains("PROYECTOS_DELETE"));
            model.addAttribute("PROYECTOS_VIEW_TEAMS", permisos.contains("PROYECTOS_VIEW_TEAMS"));
            model.addAttribute("USUARIOS_CREATE", permisos.contains("USUARIOS_CREATE"));

        }, () -> {
            model.addAttribute("AGENDA_ACCESS", false);
            model.addAttribute("AGENDA_CREATE", false);
            model.addAttribute("PROYECTOS_DELETE", false);
            model.addAttribute("PROYECTOS_VIEW_TEAMS", false);
            model.addAttribute("USUARIOS_CREATE", false);
        });

        return "home/home";
    }

    @GetMapping("/cerrar")
    public String cerrarSesion(HttpSession session) {
        session.removeAttribute("idusuario");
        return "redirect:/usuario/login";
    }

}

