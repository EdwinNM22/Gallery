package com.proyecto.galeria.controller;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.proyecto.galeria.model.Plano;
import com.proyecto.galeria.model.usuario;
import com.proyecto.galeria.service.IUsuarioService;
import com.proyecto.galeria.service.PlanoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Controller
@RequestMapping("/planos")
public class PlanosController {

    @Autowired
    private PlanoService planoService;

    @Autowired
    private IUsuarioService usuarioService;

    @GetMapping("")
    public String planos() {
        return "planos/planos";
    }



    @PostMapping("/uploadImage")
    @ResponseBody
    public ResponseEntity<String> uploadImage(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("Archivo vacío");
        }
        try {
            String uploadDir = "C:/imagenes/";
            File dir = new File(uploadDir);
            if (!dir.exists()) dir.mkdirs();

            String filename = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            Path filePath = Paths.get(uploadDir, filename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            String url = "/imagenes/" + filename;
            return ResponseEntity.ok(url);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error guardando archivo");
        }
    }

    @PostMapping("/save")
    public String guardarPlanos(@RequestBody List<Plano> planos, HttpSession session) {
        Integer userId = (Integer) session.getAttribute("idusuario");
        usuario usuario = usuarioService.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        planos.forEach(plano -> {
            plano.setUsuario(usuario);
            if (plano.getMediciones() != null) {
                plano.getMediciones().forEach(m -> m.setPlano(plano));
            }
            planoService.guardarPlanoConMediciones(plano);
        });
        return "redirect:/planos";
    }
    @GetMapping("/list")
    public String listarPlanos(Model model, HttpSession session) throws JsonProcessingException {
        Integer userId = (Integer) session.getAttribute("idusuario");
        if (userId == null) {
            return "redirect:/login";
        }
        usuario usuario = usuarioService.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        List<Plano> planos = planoService.findByUsuario(usuario);
        model.addAttribute("planos", planos);

        ObjectMapper mapper = new ObjectMapper();
        // Para evitar ciclos en la serialización, puedes configurar o crear DTOs, pero ya que no quieres DTOs:
        String planosJson = mapper.writeValueAsString(planos);
        model.addAttribute("planosJson", planosJson);

        return "planos/listPlanos";
    }
}
