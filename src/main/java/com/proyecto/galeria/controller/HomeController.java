package com.proyecto.galeria.controller;

import com.proyecto.galeria.model.album;
import com.proyecto.galeria.model.usuario;
import com.proyecto.galeria.service.IUsuarioService;
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

@Controller
@RequestMapping("/")
public class HomeController {

    private final Logger log = LoggerFactory.getLogger(HomeController.class);
    @Autowired
    private albumService albumService;
    @Autowired
    private IUsuarioService usuarioService;

    @GetMapping({"/", "/mainMenu"})
    public String mainMenu() {
        return "home/mainMenu";
    }

    @GetMapping("/home")
    public String home(Model model, HttpSession session) {
        Integer userId = Integer.parseInt(session.getAttribute("idusuario").toString());
        Optional<usuario> optionalUsuario = usuarioService.findById(userId);

        String tipoUsuario = optionalUsuario.map(usuario::getTipo_usuario).orElse("UNKNOWN");
        boolean isAdmin = "ADMIN".equals(tipoUsuario);

        model.addAttribute("isAdmin", isAdmin);
        model.addAttribute("tipoUsuario", tipoUsuario);

        List<album> albumes = albumService.findAll();
        model.addAttribute("albumes", albumes);
        model.addAttribute("sesion", session.getAttribute("idusuario"));

        log.info("Sesion del usuario: {} ", session.getAttribute("idusuario"));

        return "home/home";
    }

    @GetMapping("/cerrar")
    public String cerrarSesion(HttpSession session) {
        session.removeAttribute("idusuario");
        return "redirect:/usuario/login";
    }

}

