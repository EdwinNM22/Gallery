package com.proyecto.galeria.controller;

import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.proyecto.galeria.model.Permiso;
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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
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
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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
    public String verPlanosPorProyecto(@PathVariable Long id, Model model, HttpSession session) throws JsonProcessingException {
        Integer idUsuario = (Integer) session.getAttribute("idusuario");
        if (idUsuario == null) return "redirect:/login";

        Optional<usuario> userOpt = usuarioService.findById(idUsuario);
        if (userOpt.isEmpty()) return "redirect:/login";

        usuario user = userOpt.get();
        user.getPermisos().size();
        model.addAttribute("usuarioLogueado", user);

        Set<String> permisos = user.getPermisos().stream()
                .map(Permiso::getCodigo)
                .collect(Collectors.toSet());

        if (!permisos.contains("ESTIMATION_ACCESS")) {
            return "redirect:/NoAccess/Access";
        }

        List<Plano> planos = planoService.findByProyectoPlano(id);
        model.addAttribute("planos", planos);

        ObjectMapper mapper = new ObjectMapper();
        String planosJson = mapper.writeValueAsString(planos);
        model.addAttribute("planosJson", planosJson);

        model.addAttribute("proyectoId", id);

        // Permisos para la vista de planos
        model.addAttribute("PLANO_CREATE", permisos.contains("PLANO_CREATE"));
        model.addAttribute("PLANO_DELETE", permisos.contains("PLANO_DELETE"));

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
            String uploadDir = "/opt/Gallery/planos/";
            File dir = new File(uploadDir);
            if (!dir.exists()) dir.mkdirs();

            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null) return ResponseEntity.badRequest().body("Nombre de archivo inválido");

            String filenameBase = System.currentTimeMillis() + "_";
            String finalFilename = filenameBase + originalFilename;
            File outputFile = new File(uploadDir + finalFilename);

            // 🔥 Aquí está el cambio clave
            try (OutputStream os = new FileOutputStream(outputFile)) {
                os.write(file.getBytes());
            }

            String url = "/planos/" + finalFilename;
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

        String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
        return "redirect:https://biovizionalbums.com/planos/proyectos/" + proyectoId;

    }


    @PostMapping("/eliminar/{id}")
    @ResponseBody
    public ResponseEntity<?> eliminarPlano(@PathVariable Long id) {
        Plano plano = planoService.findById(id).orElseThrow(() -> new RuntimeException("Plano no encontrado"));

        String url = plano.getUrl();
        if (url != null && url.startsWith("/planos/")) {
            String filename = url.replace("/planos/", "");
            File file = new File("/opt/Gallery/planos/" + filename);
            if (file.exists()) file.delete();
        }

        planoService.eliminar(plano);
        return ResponseEntity.ok().build();
    }

}
