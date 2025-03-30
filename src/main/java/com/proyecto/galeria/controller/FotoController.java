package com.proyecto.galeria.controller;

import com.proyecto.galeria.model.SubAlbum;
import com.proyecto.galeria.model.album;
import com.proyecto.galeria.model.foto;
import com.proyecto.galeria.model.usuario;
import com.proyecto.galeria.service.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

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

import java.util.Date;
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
        model.addAttribute("subalbum", subAlbumService.findAll());
        return "fotos/create";
    }



    @PostMapping("/save")
    public String save(Model model, foto foto,
                       @RequestParam("img") MultipartFile file,
                       HttpSession session,
                       @RequestParam("subalbum") Integer subAlbumId) throws IOException {
        LOGGER.info("Saving foto: {}", foto);

        // Obtener el usuario de la sesión
        Optional<usuario> optionalUsuario = usuarioService.findById(Integer.parseInt(session.getAttribute("idusuario").toString()));
        if (optionalUsuario.isPresent()) {
            usuario u = optionalUsuario.get();
            foto.setUsuario(u);
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado");
        }

        // Obtener el subálbum desde la base de datos
        SubAlbum subAlbum = subAlbumService.get(subAlbumId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "SubAlbum not found"));

        // Asociar la foto al subálbum
        foto.setSubAlbum(subAlbum);
        subAlbum.getFotos().add(foto);

        // Imagen
        if (foto.getId() == null) {
            String nombrefoto = upload.saveImage(file);
            foto.setImagen(nombrefoto);
        }

        // Asignar la fecha de subida a la foto (con el tipo Date)
        Date fechaHoraSubida = new Date();  // Obtiene la fecha y hora actual
        foto.setFecha(fechaHoraSubida);  // Usando el setter para asignar el valor

        // Guardar la foto
        fotoService.save(foto);
        subAlbumService.save(subAlbum);

        // Agregar mensaje de éxito
        model.addAttribute("message", "La foto se ha agregado con éxito");

        // Redirigir
        return "redirect:/albumes/" + subAlbum.getAlbum().getId();
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
        Optional<foto> optionalFoto = fotoService.get(foto.getId());

        if (optionalFoto.isPresent()) {
            foto fotoExistente = optionalFoto.get();

            // Mantener las relaciones existentes
            foto.setUsuario(fotoExistente.getUsuario());
            foto.setSubAlbum(fotoExistente.getSubAlbum());

            // Mantener la fecha original (si no quieres que cambie)
            foto.setFecha(fotoExistente.getFecha());

            if (file.isEmpty()) {
                // Si no se seleccionó nueva imagen, mantener la imagen actual
                foto.setImagen(fotoExistente.getImagen());
            } else {
                // cuando se edita la imagen
                if (fotoExistente.getImagen() != null && !fotoExistente.getImagen().equals("default.jpg")) {
                    upload.deleteImage(fotoExistente.getImagen());
                }

                String nombrefoto = upload.saveImage(file);
                foto.setImagen(nombrefoto);
            }

            // Actualizar después de todos los cambios
            fotoService.update(foto);
        } else {
            throw new IllegalArgumentException("Foto no encontrada con el ID: " + foto.getId());
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
