package com.proyecto.galeria.controller;

import com.proyecto.galeria.model.*;
import com.proyecto.galeria.service.*;

import com.proyecto.galeria.service.Impl.UsuarioServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.*;

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
    @Autowired
    private fotoService fotoService;

    @Autowired
    private Uploadfoto upload;

    @GetMapping("")
    public String home(Model model) {
        List<album> albumes = albumService.findAll();
        model.addAttribute("albums", albumes);
        List<SubAlbum> subAlbumes = subAlbumService.findAll();
        model.addAttribute("subAlbumes", subAlbumes);

        if (!albumes.isEmpty()) {
            album ultimo = albumes.get(albumes.size() - 1);
            return "redirect:/albumes/show";
        } else {
            return "redirect:/albumes/show";
        }
    }

    @GetMapping("/show")
    public String show(Model model) {
        model.addAttribute("albumes", albumService.findAll());
        return "albumes/show";
    }

    @GetMapping("/create")
    public String albumes(Model model) {
        model.addAttribute("albumes", albumService.findAll());
        model.addAttribute("subalbum", subAlbumService.findAll());
        return "albumes/create";
    }

    @PostMapping("/save")
    public ResponseEntity<String> save(album album, HttpSession session) {
        LOGGER.info("Saving album: {}", album);

        Object idUsuarioObj = session.getAttribute("idusuario");
        if (idUsuarioObj == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuario no autenticado");
        }

        usuario u = usuarioService.findById(Integer.parseInt(idUsuarioObj.toString()))
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        album.setUsuario(u);
        album savedAlbum = albumService.save(album);

        SubAlbum subAlbumAntes = new SubAlbum(null, "Antes", "Upload the photos of the initial state of the project.", "Antes", savedAlbum, u);
        SubAlbum subAlbumDespues = new SubAlbum(null, "Después", "Upload the photos of the final state of the project.", "Despues", savedAlbum, u);

        subAlbumService.save(subAlbumAntes);
        subAlbumService.save(subAlbumDespues);

        return ResponseEntity.ok("Álbum guardado con éxito");
    }

    @GetMapping("/{id}")
    public String viewAlbum(@PathVariable Integer id, Model model, HttpSession session) {

        // 1. Verificar si el usuario es ADMIN
        Integer userId = Integer.parseInt(session.getAttribute("idusuario").toString());
        Optional<usuario> optionalUsuario = usuarioService.findById(userId);
        boolean isAdmin = optionalUsuario.map(user -> "ADMIN".equals(user.getTipo_usuario()))
                .orElse(false);
        model.addAttribute("isAdmin", isAdmin);  // Pasar a la vista

        Optional<album> optionalAlbum = albumService.get(id);
        if (optionalAlbum.isPresent()) {
            album album = optionalAlbum.get();
            List<SubAlbum> subAlbumes = album.getSubAlbumes() != null ? album.getSubAlbumes() : new ArrayList<>();

            // Subálbumes principales
            SubAlbum antes = subAlbumes.stream()
                    .filter(s -> s.getNombre().equals("Antes"))
                    .findFirst()
                    .orElse(null);

            SubAlbum despues = subAlbumes.stream()
                    .filter(s -> s.getNombre().equals("Después"))
                    .findFirst()
                    .orElse(null);

            // Agrupar subálbumes en fragmentos
            Map<String, Fragmento> fragmentosMap = new HashMap<>();

            for (SubAlbum subAlbum : subAlbumes) {
                if (subAlbum.getNombre().contains(" - ")) {
                    String[] parts = subAlbum.getNombre().split(" - ");
                    if (parts.length == 2) {
                        String fragmentName = parts[0];
                        String tipo = parts[1];
                        Fragmento fragmento = fragmentosMap.getOrDefault(fragmentName,
                                new Fragmento(fragmentName, subAlbum.getDescripcion(), new ArrayList<>()));

                        // Actualizar descripción solo si es más específica
                        if (subAlbum.getDescripcion() != null && !subAlbum.getDescripcion().isEmpty()) {
                            fragmento.setDescripcion(subAlbum.getDescripcion());
                        }

                        fragmento.getSubAlbumes().add(subAlbum);
                        fragmentosMap.put(fragmentName, fragmento);
                    }
                }
            }

            List<Fragmento> fragmentos = new ArrayList<>(fragmentosMap.values());

            model.addAttribute("album", album);
            model.addAttribute("subAlbumAntes", antes);
            model.addAttribute("subAlbumDespues", despues);
            model.addAttribute("fragmentos", fragmentos);

            return "albumes/albumes";
        } else {
            return "redirect:/";
        }
    }

    @PostMapping("/saveFragment")
    public String saveFragment(
            @RequestParam Integer albumId,
            @RequestParam(required = false) String fragmentId,
            @RequestParam String name,
            @RequestParam(required = false) String description,
            HttpSession session) {

        try {
            Object idUsuarioObj = session.getAttribute("idusuario");
            if (idUsuarioObj == null) {
                return "redirect:/login";
            }

            usuario u = usuarioService.findById(Integer.parseInt(idUsuarioObj.toString()))
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            Optional<album> optionalAlbum = albumService.get(albumId);
            if (!optionalAlbum.isPresent()) {
                return "redirect:/albumes?error=Album+not+found";
            }

            album album = optionalAlbum.get();

            if ("main".equals(fragmentId)) {
                // Actualizar descripción del fragmento principal
                List<SubAlbum> subAlbumes = album.getSubAlbumes();
                for (SubAlbum subAlbum : subAlbumes) {
                    if ("Antes".equals(subAlbum.getNombre()) || "Después".equals(subAlbum.getNombre())) {
                        subAlbum.setDescripcion(description);
                        subAlbumService.save(subAlbum);
                    }
                }
            } else if (fragmentId == null || fragmentId.isEmpty()) {
                // Crear nuevo fragmento
                SubAlbum subAlbumAntes = new SubAlbum(null, name + " - Antes",
                        description != null ? description : "Fotos del estado inicial del fragmento",
                        "Antes", album, u);

                SubAlbum subAlbumDespues = new SubAlbum(null, name + " - Después",
                        description != null ? description : "Fotos del estado final del fragmento",
                        "Despues", album, u);

                subAlbumService.save(subAlbumAntes);
                subAlbumService.save(subAlbumDespues);
            } else {
                // Editar fragmento existente
                List<SubAlbum> subAlbumes = subAlbumService.findByAlbumId(albumId);
                for (SubAlbum subAlbum : subAlbumes) {
                    if (subAlbum.getNombre().startsWith(fragmentId + " - ")) {
                        String tipo = subAlbum.getNombre().split(" - ")[1];
                        subAlbum.setNombre(name + " - " + tipo);
                        subAlbum.setDescripcion(description);
                        subAlbumService.save(subAlbum);
                    }
                }
            }

            return "redirect:/albumes/" + albumId + "?success=Fragment+saved+successfully";
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/albumes/" + albumId + "?error=Error+saving+fragment";
        }
    }

    @PostMapping("/deleteFragment/{fragmentId}")
    public String deleteFragment(
            @PathVariable String fragmentId,
            @RequestParam Integer albumId) {

        try {
            if ("main".equals(fragmentId)) {
                return "redirect:/albumes/" + albumId + "?error=Cannot+delete+main+fragment";
            }

            List<SubAlbum> subAlbumes = subAlbumService.findByAlbumId(albumId);
            for (SubAlbum subAlbum : subAlbumes) {
                if (subAlbum.getNombre().startsWith(fragmentId + " - ")) {
                    // Eliminar fotos asociadas
                    for (foto foto : subAlbum.getFotos()) {
                        if (!foto.getImagen().equals("default.jpg")) {
                            upload.deleteImage(foto.getImagen());
                        }
                        fotoService.delete(foto.getId());
                    }
                    // Eliminar subálbum
                    subAlbumService.delete(subAlbum.getId());
                }
            }

            return "redirect:/albumes/" + albumId + "?success=Fragment+deleted+successfully";
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/albumes/" + albumId + "?error=Error+deleting+fragment";
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
    public String delete(@PathVariable Integer id, HttpServletRequest request) {
        Optional<album> optionalAlbum = albumService.get(id);

        if (optionalAlbum.isPresent()) {
            album album = optionalAlbum.get();

            for (SubAlbum subAlbum : album.getSubAlbumes()) {
                for (foto foto : subAlbum.getFotos()) {
                    if (!foto.getImagen().equals("default.jpg")) {
                        upload.deleteImage(foto.getImagen());
                    }
                }
                subAlbum.getFotos().clear();
                subAlbumService.save(subAlbum);
            }

            albumService.delete(id);
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Álbum no encontrado");
        }

        // Obtiene la URL de referencia (la página actual)
        String referer = request.getHeader("Referer");
        // Si no hay referencia, redirige a /albumes por defecto
        return "redirect:" + (referer != null ? referer : "/albumes");
    }
}

// Clase Fragmento para agrupar subálbumes
