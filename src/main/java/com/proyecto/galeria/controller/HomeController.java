package com.proyecto.galeria.controller;

import com.proyecto.galeria.model.album;
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

@Controller
@RequestMapping("/")
public class HomeController {


    private final Logger log = LoggerFactory.getLogger(HomeController.class);
    @Autowired
    private albumService albumService;

    @GetMapping("")
    public String home(Model model, HttpSession session) {
        List<album> albumes = albumService.findAll(); // Obtener todos los álbumes
        model.addAttribute("albumes", albumes); // Pasar los álbumes al modelo

        log.info("Sesion del usuario: {} ", session.getAttribute("idusuario"));
        //SESSION
        model.addAttribute("sesion", session.getAttribute("idusuario"));

        return "usuario/home";

    }

    @GetMapping("/cerrar")
    public String cerrarSesion( HttpSession session) {
        session.removeAttribute("idusuario");
        return "redirect:/usuario/login";


    }

}
