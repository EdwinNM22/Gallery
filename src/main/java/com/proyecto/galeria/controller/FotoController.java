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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
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
        model.addAttribute("subalbum", subAlbumService.findAll());
        return "fotos/create";
    }



    @PostMapping("/save")
    public String save(Model model, foto foto,
                       @RequestParam("img") MultipartFile[] files,
                       HttpSession session,
                       @RequestParam("subalbum") Integer subAlbumId,
                       @RequestParam("nombre") String nombre,
                       @RequestParam(value = "hora", required = false) String hora) throws IOException {

        Optional<usuario> optionalUsuario = usuarioService.findById(Integer.parseInt(session.getAttribute("idusuario").toString()));
        if (!optionalUsuario.isPresent()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado");
        }

        usuario u = optionalUsuario.get();
        SubAlbum subAlbum = subAlbumService.get(subAlbumId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "SubAlbum not found"));

        // Establecer la fecha y hora de la foto. Si no se pasa hora, se usa la hora actual.
        Date fechaFoto = new Date();
        if (hora != null && !hora.isEmpty()) {
            // Si el usuario ingresa una hora, la parseamos y la asignamos
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
            try {
                fechaFoto = sdf.parse(hora);
            } catch (ParseException e) {
                // Si hay error en el formato, utilizamos la hora actual
                fechaFoto = new Date();
            }
        }

        for (MultipartFile file : files) {
            foto nuevaFoto = new foto();
            nuevaFoto.setUsuario(u);
            nuevaFoto.setSubAlbum(subAlbum);
            nuevaFoto.setFecha(fechaFoto); // Asignamos la fecha y hora
            nuevaFoto.setNombre(nombre); // Aquí se establece el nombre que estaba faltando

            String nombrefoto = upload.saveImage(file); // este es el nombre del archivo guardado en disco
            nuevaFoto.setImagen(nombrefoto); // este nombre sí se usa para mostrar la imagen

            fotoService.save(nuevaFoto);
            subAlbum.getFotos().add(nuevaFoto);
        }

        subAlbumService.save(subAlbum);
        model.addAttribute("message", "Fotos subidas correctamente");

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
