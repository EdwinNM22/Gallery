package com.proyecto.galeria.controller;

import com.proyecto.galeria.model.album;


import com.proyecto.galeria.model.usuario;
import com.proyecto.galeria.service.IUsuarioService;
import com.proyecto.galeria.service.albumService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/adm")
public class AdmController {


    @Autowired
    private albumService albumService;

    @Autowired
    private IUsuarioService usuarioService;



    @GetMapping("")
    public String home(Model model, HttpSession session) {
        List<album> albumes = albumService.findAll();
        model.addAttribute("albumes", albumes);

        // Obtener el rol del usuario desde la sesión
        Object idUsuarioObj = session.getAttribute("idusuario");
        if (idUsuarioObj != null) {
            Optional<usuario> user = usuarioService.findById(Integer.parseInt(idUsuarioObj.toString()));
            if (user.isPresent()) {
                model.addAttribute("tipoUsuario", user.get().getTipo_usuario());
                model.addAttribute("isAdmin", "ADMIN".equals(user.get().getTipo_usuario()));
                model.addAttribute("isSupervisorPlus", "SUPERVISORPLUS".equals(user.get().getTipo_usuario()));
            }
        }

        return "adm/home";
    }


}


