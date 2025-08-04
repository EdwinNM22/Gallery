package com.proyecto.galeria.controller;

import com.proyecto.galeria.model.ProyectoPlano;
import com.proyecto.galeria.repository.ProyectoPlanoRepository;
import com.proyecto.galeria.service.IUsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;


import javax.servlet.http.HttpSession;
import java.util.List;

@Controller
@RequestMapping("/proyectosPlano")
public class ProyectoPlanoController {

    @Autowired
    private ProyectoPlanoRepository proyectoPlanoRepository;

    @Autowired
    private IUsuarioService usuarioService;

    @GetMapping
    public String listarProyectos(Model model) {
        List<ProyectoPlano> proyectos = proyectoPlanoRepository.findAll();
        model.addAttribute("proyectos", proyectos);
        model.addAttribute("nuevoProyecto", new ProyectoPlano());
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
    public String eliminarProyecto(@PathVariable Long id) {
        proyectoPlanoRepository.deleteById(id);
        return "redirect:/proyectosPlano";
    }
}