package com.proyecto.galeria.controller;

import com.proyecto.galeria.model.SubAlbum;
import com.proyecto.galeria.model.foto;
import com.proyecto.galeria.model.usuario;
import com.proyecto.galeria.service.fotoService;
import com.proyecto.galeria.service.subAlbumService;
import com.proyecto.galeria.service.albumService;
import com.proyecto.galeria.service.UsuarioServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Optional;


@Controller
@RequestMapping("/subAlbumes")
public class SubAlbumController {

    private final Logger LOGGER = LoggerFactory.getLogger(SubAlbumController.class);
    private final subAlbumService subAlbumService;

    // Constructor para inyectar el servicio
    public SubAlbumController(subAlbumService subAlbumService) {
        this.subAlbumService = subAlbumService;
    }

    @GetMapping("/antes/{id}")
    public String showAntes(@PathVariable("id") Integer id, Model model) {
        Optional<SubAlbum> subAlbum = subAlbumService.get(id);
        if (subAlbum.isPresent()) {
            model.addAttribute("subAlbum", subAlbum.get());
            return "subAlbumes/subalbumAntes";  // Vista para el subálbum 'Antes'
        } else {
            return "redirect:/subAlbumes";  // Redirigir si no se encuentra el subálbum
        }
    }

    @GetMapping("/despues/{id}")
    public String showDespues(@PathVariable("id") Integer id, Model model) {
        Optional<SubAlbum> subAlbum = subAlbumService.get(id);
        if (subAlbum.isPresent()) {
            model.addAttribute("subAlbum", subAlbum.get());
            return "subAlbumes/subalbumDespues";  // Vista para el subálbum 'Después'
        } else {
            return "redirect:/subAlbumes";  // Redirigir si no se encuentra el subálbum
        }
    }
}










