package com.proyecto.galeria.controller;
import com.proyecto.galeria.model.SubAlbum;
import com.proyecto.galeria.model.foto;
import com.proyecto.galeria.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import java.util.List;
import java.util.Optional;


@Controller
@RequestMapping("/subAlbumes")
public class SubAlbumController {

    private final Logger LOGGER = LoggerFactory.getLogger(SubAlbumController.class);
    private final subAlbumService subAlbumService;

    @Autowired
    private  fotoService fotoService;

    @Autowired
    private albumService albumService;


    @Autowired
    private IUsuarioService iUsuarioService;


    // Constructor para inyectar el servicio
    public SubAlbumController(subAlbumService subAlbumService) {
        this.subAlbumService = subAlbumService;
    }


    @GetMapping("/antes/{id}")
    public String showAntes(@PathVariable("id") Integer id, Model model) {
        // Obtener solo los subálbumes de tipo "antes"
        List<SubAlbum> subAlbumesAntes = subAlbumService.getSubAlbumesAntes();

        // Buscar el subálbum específico por id
        SubAlbum subAlbumAntes = subAlbumesAntes.stream()
                .filter(subAlbum -> subAlbum.getId().equals(id))
                .findFirst()
                .orElse(null);

        // Si se encuentra el subálbum, obtener sus fotos
        if (subAlbumAntes != null) {
            model.addAttribute("foto", subAlbumAntes.getFotos()); // Asumiendo que SubAlbum tiene el método getFotos()
        }

        return showSubAlbum(id, model, "subAlbumes/subalbumAntes");
    }

    @GetMapping("/despues/{id}")
    public String showDespues(@PathVariable("id") Integer id, Model model) {
        // Obtener solo los subálbumes de tipo "despues"
        List<SubAlbum> subAlbumesDespues = subAlbumService.getSubAlbumesDespues();

        // Buscar el subálbum específico por id
        SubAlbum subAlbumDespues = subAlbumesDespues.stream()
                .filter(subAlbum -> subAlbum.getId().equals(id))
                .findFirst()
                .orElse(null);

        // Si se encuentra el subálbum, obtener sus fotos
        if (subAlbumDespues != null) {
            model.addAttribute("foto", subAlbumDespues.getFotos()); // Asumiendo que SubAlbum tiene el método getFotos()
        }

        return "subAlbumes/subalbumDespues";
    }

    private String showSubAlbum(Integer id, Model model, String viewName) {
        Optional<SubAlbum> subAlbum = subAlbumService.get(id);
        if (subAlbum.isPresent()) {
            model.addAttribute("subAlbum", subAlbum.get());

            // Obtener las fotos relacionadas con el SubAlbum
            List<foto> fotos = subAlbum.get().getFotos();  // Usando la relación de ManyToMany

            // Agregar las fotos al modelo
            model.addAttribute("foto", fotos);

            return viewName;
        } else {
            return "redirect:/subAlbumes";
        }
    }



}










