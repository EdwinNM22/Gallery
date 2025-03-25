package com.proyecto.galeria.controller;

import com.proyecto.galeria.model.album;
import com.proyecto.galeria.model.usuario;
import com.proyecto.galeria.service.UsuarioServiceImpl;
import com.proyecto.galeria.service.albumService;

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
@RequestMapping("/albumes")
public class AlbumController {

    private final Logger LOGGER = LoggerFactory.getLogger(AlbumController.class);

    @Autowired
    private albumService albumService;

    @Autowired
    UsuarioServiceImpl usuarioService;


    @GetMapping("")
    public String home(Model model) {
        List<album> albumes = albumService.findAll(); // Obtener todos los álbumes
        model.addAttribute("albumes", albumes); // Pasar los álbumes al modelo
        return "albumes/show"; // Asegúrate de que esta vista esté configurada para mostrar álbumes
    }

    @GetMapping("/create")
    public String create() {
        return "albumes/create";
    }

    @PostMapping("/save")
    public String save(album album, HttpSession session) {
        LOGGER.info("Saving album: {}", album);

        Object idUsuarioObj = session.getAttribute("idusuario");
        if (idUsuarioObj == null) {
            // Manejar el error o redirigir al usuario a una página de login
            return "redirect:/login"; // O alguna otra página de error
        }

        usuario u = usuarioService.findById(Integer.parseInt(idUsuarioObj.toString())).get();
        album.setUsuario(u);

        albumService.save(album);

        return "redirect:/albumes";
    }


    @GetMapping("/{id}")
    public String viewAlbum(@PathVariable Integer id, Model model) {
        Optional<album> optionalAlbum = albumService.get(id);
        if (optionalAlbum.isPresent()) {
            model.addAttribute("album", optionalAlbum.get());
            return "albumes/albumes"; // Nombre de la vista para mostrar las fotos del álbum
        } else {
            return "redirect:/"; // Redirigir a la página de inicio si no se encuentra el álbum
        }
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Integer id, Model model) {
        Optional<album> optionalAlbum = albumService.get(id);

        LOGGER.info("search album: {}", optionalAlbum);
        if (optionalAlbum.isPresent()) {
            model.addAttribute("album", optionalAlbum.get());
            return "albumes/edit";
        } else {
            return "redirect:/albumes";
        }
    }

    @PostMapping("/update")
    public String update(album album) {
        LOGGER.info("Updating album: {}", album);
        albumService.update(album);
        return "redirect:/albumes";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Integer id) {
        Optional<album> optionalAlbum = albumService.get(id);

        if (optionalAlbum.isPresent()) {
            albumService.delete(id);
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Album not found");
        }

        return "redirect:/albumes";
    }
}
