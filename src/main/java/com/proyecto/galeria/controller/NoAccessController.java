package com.proyecto.galeria.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/NoAccess")
public class NoAccessController {

    @GetMapping("/Access")
    public String accessDenied() {
        return "noaccess/access";
    }
}
