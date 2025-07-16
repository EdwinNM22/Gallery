package com.proyecto.galeria.controller;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("")
public class SelectController {


    @RequestMapping("/")
    public String selectSystem() {
        return "home/select";
    }
}

