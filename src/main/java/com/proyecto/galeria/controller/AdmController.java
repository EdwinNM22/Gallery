package com.proyecto.galeria.controller;

import com.proyecto.galeria.model.album;


import com.proyecto.galeria.service.IUsuarioService;
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

    @Autowired
    private IUsuarioService usuarioService;



    @GetMapping("")
    public String home(Model model) {

        List<album> albumes = albumService.findAll();
        model.addAttribute("albumes", albumes); //aqui le cambie

        return "adm/home";
    }
    @GetMapping("/usuarios")
    public String usuarios(Model model) {
        model.addAttribute("usuarios", usuarioService.findAll());
        return "adm/home";
    }

}


