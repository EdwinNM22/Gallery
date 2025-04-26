package com.proyecto.galeria.controller;

import com.proyecto.galeria.model.SubAlbum;
import com.proyecto.galeria.model.album;
import com.proyecto.galeria.model.foto;
import com.proyecto.galeria.model.usuario;
import com.proyecto.galeria.service.*;


import com.proyecto.galeria.service.Impl.UsuarioServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

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
    @ResponseBody
    public ResponseEntity<Map<String, String>> save(
            @RequestParam("img") MultipartFile[] files,
            HttpSession session,
            @RequestParam("subalbum") Integer subAlbumId,
            @RequestParam("nombre") String nombre,
            @RequestParam(value = "hora", required = false) String hora) throws IOException {

        // Validación inicial rápida
        if (files == null || files.length == 0) {
            return ResponseEntity.badRequest().body(Map.of("error", "No files provided"));
        }

        // Obtener usuario (caché esta operación si se repite mucho)
        Integer userId = Integer.parseInt(session.getAttribute("idusuario").toString());
        Optional<usuario> optionalUsuario = usuarioService.findById(userId);
        if (!optionalUsuario.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Usuario no encontrado"));
        }

        // Obtener subálbum una sola vez
        SubAlbum subAlbum = subAlbumService.get(subAlbumId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "SubAlbum not found"));

        // Procesar fecha una sola vez
        Date fechaFoto = processFecha(hora);
        usuario u = optionalUsuario.get();

        // Procesar archivos en paralelo
        List<foto> fotos = Arrays.stream(files)
                .parallel()
                .map(file -> {
                    try {
                        foto nuevaFoto = new foto();
                        nuevaFoto.setUsuario(u);
                        nuevaFoto.setSubAlbum(subAlbum);
                        nuevaFoto.setFecha(fechaFoto);
                        nuevaFoto.setNombre(nombre);
                        String nombrefoto = upload.saveImage(file);
                        nuevaFoto.setImagen(nombrefoto);
                        return nuevaFoto;
                    } catch (IOException e) {
                        throw new RuntimeException("Error processing file: " + file.getOriginalFilename(), e);
                    }
                })
                .collect(Collectors.toList());

        // Guardar todas las fotos de una vez (implementa batch save en tu servicio si es posible)
        fotoService.saveAll(fotos);

        // Actualizar subálbum
        subAlbum.getFotos().addAll(fotos);
        subAlbumService.save(subAlbum);

        return ResponseEntity.ok(Map.of("message", "Photos or videos uploaded successfully"));
    }

    private Date processFecha(String hora) {
        if (hora != null && !hora.isEmpty()) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                String fechaCompleta = new SimpleDateFormat("dd/MM/yyyy").format(new Date()) + " " + hora;
                return sdf.parse(fechaCompleta);
            } catch (ParseException e) {
                return new Date();
            }
        }
        return new Date();
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


    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/delete/{id}") // Cambiado a @DeleteMapping
    @ResponseBody // Añadido para devolver respuesta JSON
    public ResponseEntity<Map<String, String>> delete(@PathVariable Integer id) {
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
            return ResponseEntity.ok(Map.of("message", "Foto eliminada correctamente"));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Foto no encontrada"));
        }
    }
}
