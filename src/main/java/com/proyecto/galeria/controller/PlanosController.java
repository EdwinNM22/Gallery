package com.proyecto.galeria.controller;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.proyecto.galeria.model.Plano;
import com.proyecto.galeria.model.ProyectoPlano;
import com.proyecto.galeria.model.usuario;
import com.proyecto.galeria.repository.ProyectoPlanoRepository;
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
import java.util.List;
import java.util.Map;

import org.apache.commons.imaging.ImageFormats;
import org.apache.commons.imaging.Imaging;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

@Controller
@RequestMapping("/planos")
public class PlanosController {

    @Autowired
    private PlanoService planoService;

    @Autowired
    private IUsuarioService usuarioService;


    @Autowired
    private ProyectoPlanoRepository proyectoPlanoRepository;


    @GetMapping("/proyectos/{id}")
    public String verPlanosPorProyecto(@PathVariable Long id, Model model) throws JsonProcessingException {
        List<Plano> planos = planoService.findByProyectoPlano(id);
        model.addAttribute("planos", planos);

        ObjectMapper mapper = new ObjectMapper();
        String planosJson = mapper.writeValueAsString(planos);
        model.addAttribute("planosJson", planosJson);

        model.addAttribute("proyectoId", id);

        return "planos/viewPlanos";
    }


    // Endpoint para crear planos
    @GetMapping("")
    public String planos(@RequestParam(value = "proyectoId", required = false) Long proyectoId, Model model) {
        model.addAttribute("proyectoId", proyectoId);
        return "planos/planos"; // tu vista para crear plano
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

            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename.substring(originalFilename.lastIndexOf('.') + 1).toLowerCase();
            String filenameBase = System.currentTimeMillis() + "_";
            File outputFile;
            String finalFilename;

            if ("heic".equals(extension) || "heif".equals(extension)) {
                BufferedImage bufferedImage = Imaging.getBufferedImage(file.getInputStream());
                finalFilename = filenameBase + originalFilename.replaceAll("\\.(heic|heif)$", ".jpg");
                outputFile = new File(uploadDir + finalFilename);
                ImageIO.write(bufferedImage, "jpg", outputFile);
            } else {
                finalFilename = filenameBase + originalFilename;
                outputFile = new File(uploadDir + finalFilename);
                file.transferTo(outputFile);
            }

            String url = "/imagenes/" + finalFilename;
            return ResponseEntity.ok(url);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error guardando archivo");
        }
    }

    @PostMapping("/save")
    public String guardarPlanos(@RequestParam Long proyectoId,
                                @RequestBody List<Plano> planos,
                                HttpSession session) {
        Integer userId = (Integer) session.getAttribute("idusuario");
        usuario usuario = usuarioService.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        ProyectoPlano proyecto = proyectoPlanoRepository.findById(proyectoId)
                .orElseThrow(() -> new RuntimeException("ProyectoPlano no encontrado"));

        planos.forEach(plano -> {
            plano.setUsuario(usuario);
            plano.setProyectoPlano(proyecto);
            if (plano.getMediciones() != null) {
                plano.getMediciones().forEach(m -> m.setPlano(plano));
            }
            planoService.guardarPlanoConMediciones(plano);
        });

        return "redirect:/planos/proyectos/" + proyectoId;
    }





}
