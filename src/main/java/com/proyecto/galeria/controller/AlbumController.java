package com.proyecto.galeria.controller;

import com.proyecto.galeria.model.SubAlbum;
import com.proyecto.galeria.model.album;
import com.proyecto.galeria.model.usuario;
import com.proyecto.galeria.service.UsuarioServiceImpl;
import com.proyecto.galeria.service.albumService;

import com.proyecto.galeria.service.subAlbumService;
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
import java.util.stream.Collectors;

@Controller
@RequestMapping("/albumes")
public class AlbumController {

    private final Logger LOGGER = LoggerFactory.getLogger(AlbumController.class);
    @Autowired
    private albumService albumService;
    @Autowired
    UsuarioServiceImpl usuarioService;
    @Autowired
    private subAlbumService subAlbumService;

    @GetMapping("")
    public String home(Model model) {

        List<album> albumes = albumService.findAll();
        model.addAttribute("albums", albumes);


//        return "albumes/show"; aqui va el show de albumes
        List<SubAlbum> subAlbumes = subAlbumService.findAll();
        model.addAttribute("subAlbumes", subAlbumes);

        if (!albumes.isEmpty()) {
            album ultimo = albumes.get(albumes.size() - 1);
            return "redirect:/albumes/" + ultimo.getId();
        } else {
            return "redirect:/albumes/vacio";
        }

    }

    @GetMapping("/show")
    public String show(Model model ){
        model.addAttribute("albumes", albumService.findAll());
        return "albumes/show";
    }

    @GetMapping("/create")
    public String albumes(Model model) {
//        model.addAttribute("albumes", albumService.findAll());  // Lista de albumes
//        model.addAttribute("subalbum", subAlbumService.findAll());  // Lista de subálbumes
        return "albumes/create";  // La vista donde se muestra el modal
    }

    @PostMapping("/save")
    public String save(album album, HttpSession session) {
        LOGGER.info("Saving album: {}", album);

        // Obtener el id del usuario de la sesión
        Object idUsuarioObj = session.getAttribute("idsuaurio");
        if (idUsuarioObj == null) {
            return "redirect:/login";
        }

        // Obtener el usuario desde la base de datos
        usuario u = usuarioService.findById(Integer.parseInt(idUsuarioObj.toString())).get();

        // Asocia el usuario al álbum
        album.setUsuario(u);

        // Guardar el álbum y obtener la referencia guardada
        album savedAlbum = albumService.save(album);

        // Crear los subálbumes asociados al álbum
        SubAlbum subAlbumAntes = new SubAlbum(null, "Antes", "Subálbum creado por default", "Antes", savedAlbum, u);
        SubAlbum subAlbumDespues = new SubAlbum(null, "Después", "Subálbum creado por default", "Despues", savedAlbum, u);

        // Guardar los subálbumes
        subAlbumService.save(subAlbumAntes);
        subAlbumService.save(subAlbumDespues);

        return "redirect:/albumes/create";
    }

    @GetMapping("/{id}")
    public String viewAlbum(@PathVariable Integer id, Model model) {
        Optional<album> optionalAlbum = albumService.get(id);
        if (optionalAlbum.isPresent()) {
            album album = optionalAlbum.get();
            List<SubAlbum> subAlbumes = album.getSubAlbumes(); // Esto ya trae solo los subálbumes del álbum actual

            // Buscar los subálbumes específicos
            SubAlbum antes = subAlbumes.stream()
                    .filter(s -> s.getNombre().equals("Antes"))
                    .findFirst()
                    .orElse(null);

            SubAlbum despues = subAlbumes.stream()
                    .filter(s -> s.getNombre().equals("Después"))
                    .findFirst()
                    .orElse(null);

            // Agregar al modelo
            model.addAttribute("album", album);
            model.addAttribute("subAlbumAntes", antes); // Objeto único (no lista)
            model.addAttribute("subAlbumDespues", despues); // Objeto único (no lista)

            return "albumes/albumes";
        } else {
            return "redirect:/";
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
