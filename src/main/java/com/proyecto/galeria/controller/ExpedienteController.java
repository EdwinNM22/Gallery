package com.proyecto.galeria.controller;

import com.proyecto.galeria.model.Expediente;
import com.proyecto.galeria.model.usuario;
import com.proyecto.galeria.service.ExpedienteService;
import com.proyecto.galeria.service.IUsuarioService;
import com.proyecto.galeria.service.PermisoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/expediente")
public class ExpedienteController {

    @Autowired
    private ExpedienteService expedienteService;

    @Autowired
    private IUsuarioService usuarioService;

    @Autowired
    private PermisoService permisoService;

    @GetMapping("")
    public String expedienteTable(Model model, HttpSession session) {
        Integer idUsuario = (Integer) session.getAttribute("idusuario");
        Optional<usuario> optionalUsuario = usuarioService.findById(idUsuario);

        // Control de permisos
        if (optionalUsuario.isEmpty() || optionalUsuario.get().getPermisos().stream()
                .noneMatch(p -> "EXPEDIENTE_ACCESS".equals(p.getCodigo()))) {
            return "redirect:/NoAccess/Access";
        }

        usuario usuario = optionalUsuario.get();

        // Obtener expedientes según tipo usuario (ej: "EDGAR" ve todos)
        List<Expediente> expedientes =
                ("EDGAR".equalsIgnoreCase(usuario.getTipo_usuario())
                        || usuario.getPermisos().stream().anyMatch(p -> "EXPEDIENTE_VIEW_ALL".equals(p.getCodigo())))
                        ? expedienteService.findAll()
                        : expedienteService.findByUsuario(usuario);

        model.addAttribute("expedientes", expedientes);
        model.addAttribute("expediente", new Expediente());
        model.addAttribute("usuarios", usuarioService.findAll());
        model.addAttribute("abrirModalEditar", false);


        model.addAttribute("permisos", permisoService.getAllPermisos());
        model.addAttribute("permisosAgrupados", permisoService.getPermisosAgrupadosPorVista());

        // Forzar carga permisos y agregarlos al modelo
        usuario.getPermisos().size();

        Set<String> permisos = usuario.getPermisos().stream()
                .map(p -> p.getCodigo())
                .collect(Collectors.toSet());

        model.addAttribute("EXPEDIENTE_ACCESS", permisos.contains("EXPEDIENTE_ACCESS"));
        model.addAttribute("EXPEDIENTE_CREATE", permisos.contains("EXPEDIENTE_CREATE"));
        model.addAttribute("EXPEDIENTE_EDIT", permisos.contains("EXPEDIENTE_EDIT"));
        model.addAttribute("EXPEDIENTE_DELETE", permisos.contains("EXPEDIENTE_DELETE"));
        model.addAttribute("EXPEDIENTE_FORM_CREATE", permisos.contains("EXPEDIENTE_FORM_CREATE"));
        model.addAttribute("EXPEDIENTE_FORM_PDF", permisos.contains("EXPEDIENTE_FORM_PDF"));
        model.addAttribute("EXPEDIENTE_FORM_EDIT", permisos.contains("EXPEDIENTE_FORM_EDIT"));
        model.addAttribute("EXPEDIENTE_FORM_DELETE", permisos.contains("EXPEDIENTE_FORM_DELETE"));

        return "expediente/list";
    }


    // Guardar nuevo expediente
    @PostMapping("/guardar")
    public String guardarExpediente(@ModelAttribute Expediente expediente) {
        expedienteService.save(expediente);
        return "redirect:/expediente";
    }

    // Mostrar modal para editar (carga expediente y abre modal)
    @GetMapping("/editar/{id}")
    public String editarExpedienteModal(@PathVariable Integer id, Model model) {
        Expediente expediente = expedienteService.findById(id);
        if (expediente == null) {
            return "redirect:/expediente";
        }

        model.addAttribute("expediente", expediente);
        model.addAttribute("usuarios", usuarioService.findAll());
        model.addAttribute("abrirModalEditar", true);
        model.addAttribute("expedientes", expedienteService.findAll());

        // Faltaban estos ↓↓↓
        model.addAttribute("permisos", permisoService.getAllPermisos());
        model.addAttribute("permisosAgrupados", permisoService.getPermisosAgrupadosPorVista());

        return "expediente/list";
    }
    @GetMapping("/api/expediente/{id}")
    @ResponseBody
    public Expediente getExpedienteJson(@PathVariable Integer id) {
        return expedienteService.findById(id);
    }

    // Procesar edición
    @PostMapping("/editar")
    public String editarExpediente(@ModelAttribute Expediente expediente) {
        expedienteService.save(expediente);
        return "redirect:/expediente";
    }

    // Eliminar expediente
    @GetMapping("/eliminar/{id}")
    public String eliminarExpediente(@PathVariable Integer id) {
        expedienteService.deleteById(id);
        return "redirect:/expediente";
    }
}
