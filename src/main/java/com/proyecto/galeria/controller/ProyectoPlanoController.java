package com.proyecto.galeria.controller;

import com.proyecto.galeria.model.Permiso;
import com.proyecto.galeria.model.Plano;
import com.proyecto.galeria.model.ProyectoPlano;
import com.proyecto.galeria.model.usuario;
import com.proyecto.galeria.repository.ProyectoPlanoRepository;
import com.proyecto.galeria.service.IUsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;


import javax.servlet.http.HttpSession;
import javax.transaction.Transactional;
import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/proyectosPlano")
public class ProyectoPlanoController {

    @Autowired
    private ProyectoPlanoRepository proyectoPlanoRepository;

    @Autowired
    private IUsuarioService usuarioService;

    @GetMapping
    public String listarProyectos(Model model, HttpSession session) {
        Integer idUsuario = (Integer) session.getAttribute("idusuario");
        if (idUsuario == null) return "redirect:/login";

        Optional<usuario> userOpt = usuarioService.findById(idUsuario);
        if (userOpt.isEmpty()) return "redirect:/login";

        usuario user = userOpt.get();
        user.getPermisos().size(); // Forzar carga permisos
        model.addAttribute("usuarioLogueado", user);

        Set<String> permisos = user.getPermisos().stream()
                .map(Permiso::getCodigo)
                .collect(Collectors.toSet());

        if (!permisos.contains("ESTIMATION_ACCESS")) {
            return "redirect:/NoAccess/Access";
        }

        List<ProyectoPlano> proyectos = proyectoPlanoRepository.findAll();
        model.addAttribute("proyectos", proyectos);
        model.addAttribute("nuevoProyecto", new ProyectoPlano());

        // Permisos individuales para la vista
        model.addAttribute("ESTIMACION_CREATE", permisos.contains("ESTIMACION_CREATE"));
        model.addAttribute("ESTIMACION_EDIT", permisos.contains("ESTIMACION_EDIT"));
        model.addAttribute("ESTIMACION_DELETE", permisos.contains("ESTIMACION_DELETE"));
        model.addAttribute("PLANO_CREATE", permisos.contains("PLANO_CREATE"));
        model.addAttribute("PLANO_DELETE", permisos.contains("PLANO_DELETE"));

        return "planos/listPlanos";
    }


    @PostMapping("/crear")
    public String crearProyecto(@ModelAttribute ProyectoPlano proyecto, HttpSession session) {
        Integer idUsuario = (Integer) session.getAttribute("idusuario");
        usuarioService.findById(idUsuario).ifPresent(proyecto::setUsuario);
        proyectoPlanoRepository.save(proyecto);
        return "redirect:/proyectosPlano";
    }

    @GetMapping("/editar/{id}")
    public String mostrarEditar(@PathVariable Long id, Model model) {
        ProyectoPlano proyecto = proyectoPlanoRepository.findById(id).orElseThrow();
        model.addAttribute("proyectoEdit", proyecto);
        List<ProyectoPlano> proyectos = proyectoPlanoRepository.findAll();
        model.addAttribute("proyectos", proyectos);
        model.addAttribute("nuevoProyecto", new ProyectoPlano());
        return "planos/listPlanos";
    }

    @PostMapping("/editar")
    public String editarProyecto(@ModelAttribute("proyectoEdit") ProyectoPlano proyectoEdit, HttpSession session) {
        Integer idUsuario = (Integer) session.getAttribute("idusuario");
        usuarioService.findById(idUsuario).ifPresent(proyectoEdit::setUsuario);
        proyectoPlanoRepository.save(proyectoEdit);
        return "redirect:/proyectosPlano";
    }


    @PostMapping("/eliminar/{id}")
    @ResponseBody
    @Transactional
    public ResponseEntity<?> eliminarProyecto(@PathVariable Long id) {
        proyectoPlanoRepository.findById(id).ifPresent(proyecto -> {
            proyecto.getPlanos().size();
            for (Plano plano : proyecto.getPlanos()) {
                String url = plano.getUrl();
                if (url != null && url.startsWith("/planos/")) {
                    String filename = url.replace("/planos/", "");
                    File archivo = new File("/opt/Gallery/planos/" + filename);
                    if (archivo.exists()) archivo.delete();
                }
            }
            proyectoPlanoRepository.delete(proyecto);
        });
        return ResponseEntity.ok().build();
    }

}