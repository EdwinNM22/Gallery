package com.proyecto.galeria.controller;


import com.proyecto.galeria.model.album;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/supervi")
public class SuperviController {

    @GetMapping("")
    public String home(Model model) {

        return "supervi/home";
    }
}
