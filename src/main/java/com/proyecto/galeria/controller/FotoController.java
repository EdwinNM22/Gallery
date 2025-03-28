package com.proyecto.galeria.controller;

import com.proyecto.galeria.model.SubAlbum;
import com.proyecto.galeria.model.album;
import com.proyecto.galeria.model.foto;
import com.proyecto.galeria.model.usuario;
import com.proyecto.galeria.service.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/fotos")
public class FotoController {

    private final Logger LOGGER = LoggerFactory.getLogger(FotoController.class);

    @Autowired
    private fotoService fotoService;

    @Autowired
    private UsuarioServiceImpl usuarioService;

    @Autowired
    private Uploadfoto upload;

    @Autowired
    private subAlbumService subAlbumService;

    @Autowired
    private albumService albumService;  // Añadir esta línea

    @GetMapping("")
    public String show(Model model) {
        model.addAttribute("fotos", fotoService.findAll());
        return "fotos/show";
    }

    @GetMapping("/create")
    public String create(Model model) {
        model.addAttribute("albumes", albumService.findAll());  // Ahora puedes usar albumService

        List<SubAlbum> subAlbumes = subAlbumService.findAll();   // En teoria muestra todos los subAlbumes
        model.addAttribute("SubAlbum", subAlbumes);
        return "fotos/create";
    }

    @PostMapping("/save")
    public String save(foto foto,
                       @RequestParam("img") MultipartFile file,
                       HttpSession session,
                       @RequestParam("SubAlbum") Integer subAlbumId) throws IOException {
        LOGGER.info("Saving foto: {}", foto);

        // Obtener el usuario de la sesión
        usuario u = usuarioService.findById(Integer.parseInt(session.getAttribute("idusuario").toString())).get();
        foto.setUsuario(u);

        // Obtener el subálbum desde la base de datos
        SubAlbum subAlbum = subAlbumService.get(subAlbumId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "SubAlbum not found"));

        // Asociar la foto al subálbum
        foto.setSubAlbum(subAlbum);

        // Imagen
        if (foto.getId() == null) {
            String nombrefoto = upload.saveImage(file);
            foto.setImagen(nombrefoto);
        } else {
            // Código para editar imagen si es necesario
        }

        // Guardar la foto (esto guardará la relación foto-subálbum)
        fotoService.save(foto);

        return "redirect:/fotos"; // Redirigir a la página de fotos
    }


    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Integer id, Model model) {
        Optional<foto> optionalFoto = fotoService.get(id);

        LOGGER.info("search foto: {}", optionalFoto);
        // Si el objeto foto existe, lo agregamos al modelo
        optionalFoto.ifPresent(foto -> model.addAttribute("foto", foto));

        return "fotos/edit";
    }

    @PostMapping("/update")
    public String update(foto foto, @RequestParam("img") MultipartFile file) throws IOException {
        fotoService.update(foto);

        if (file.isEmpty()) {
            foto p = fotoService.get(foto.getId()).get();
            foto.setImagen(p.getImagen());
        } else { // cuando se edita la imagen
            foto p = fotoService.get(foto.getId()).get();
            // eliminar cuando no sea la imagen por defecto
            if (p.getImagen().equals("default.jpg")) {
                upload.deleteImage(p.getImagen());
            }

            String nombrefoto = upload.saveImage(file);
            foto.setImagen(nombrefoto);
        }

        return "redirect:/fotos";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Integer id) {
        Optional<foto> optionalFoto = fotoService.get(id);

        if (optionalFoto.isPresent()) {
            foto p = optionalFoto.get();

            // Eliminar la foto de todos los álbumes
            for (album album : p.getAlbumes()) {
                album.getFotos().remove(p);
            }

            // Eliminar la imagen del sistema de archivos si no es la imagen por defecto
            if (!p.getImagen().equals("default.jpg")) {
                upload.deleteImage(p.getImagen());
            }


            fotoService.delete(id);
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Foto no encontrada");
        }

        return "redirect:/fotos";
    }
}
