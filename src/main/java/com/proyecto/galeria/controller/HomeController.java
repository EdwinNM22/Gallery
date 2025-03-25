package com.proyecto.galeria.controller;

import com.proyecto.galeria.model.album;
import com.proyecto.galeria.service.albumService;

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

    @Autowired
    private albumService albumService;

    @GetMapping("")
    public String home(Model model, HttpSession session) {
        List<album> albumes = albumService.findAll(); // Obtener todos los álbumes
        model.addAttribute("albumes", albumes); // Pasar los álbumes al modelo

        //SESSION
        model.addAttribute("sesion", session.getAttribute("idusuario"));

        return "usuario/home"; // Asegúrate de que esta vista esté configurada para mostrar álbumes

    }

    @GetMapping("/cerrar")
    public String cerrarSesion(Model model, HttpSession session) {
        session.removeAttribute("idusuario");
        return "redirect:/usuario/login";


    }

}
