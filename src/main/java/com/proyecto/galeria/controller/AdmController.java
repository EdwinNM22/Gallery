package com.proyecto.galeria.controller;

import com.proyecto.galeria.model.album;


import com.proyecto.galeria.service.albumService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/adm")
public class AdmController {


    @Autowired
    private albumService albumService;

    @GetMapping("")
    public String home(Model model) {
        List<album> albumes = albumService.findAll(); // Obtener todos los álbumes
        model.addAttribute("albumes", albumes); // Pasar los álbumes al modelo
        return "adm/home"; // Asegúrate de que esta vista esté configurada para mostrar álbumes
    }
}


