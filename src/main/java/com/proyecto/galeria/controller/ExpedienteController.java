package com.proyecto.galeria.controller;

import com.proyecto.galeria.model.Expediente;
import com.proyecto.galeria.model.Form;
import com.proyecto.galeria.model.usuario;
import com.proyecto.galeria.service.ExpedienteService;
import com.proyecto.galeria.service.FormService;
import com.proyecto.galeria.service.IUsuarioService;
import com.proyecto.galeria.service.PermisoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@Controller
@RequestMapping("/expediente")
public class ExpedienteController {
    @Autowired
    private FormService formService; 

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

    @GetMapping("/selection/{id}")
    public String selection(@PathVariable("id") Integer id, Model model, HttpSession session) {
        Integer idUsuario = (Integer) session.getAttribute("idusuario");
        Optional<usuario> optionalUsuario = usuarioService.findById(idUsuario);
    
        // Control de permisos
        if (optionalUsuario.isEmpty() || optionalUsuario.get().getPermisos().stream()
                .noneMatch(p -> "EXPEDIENTE_ACCESS".equals(p.getCodigo()))) {
            return "redirect:/NoAccess/Access";
        }
    
        usuario usuario = optionalUsuario.get();
    
        // Get future projects for this expediente
        List<Form> futureForms = formService.findByExpedienteIdAndFuturo(id, true);
        
        List<Map<String, String>> events = futureForms.stream()
            .filter(f -> f.getFechaEvaluacion() != null)
            .map(f -> Map.of(
                "id", f.getId().toString(),
                "start", f.getFechaEvaluacion().toString(),
                "end",   f.getFechaEvaluacion().toString(),
                "nombreCliente", f.getNombreCliente()    // assuming getter exists
            ))
            .collect(Collectors.toList());

        model.addAttribute("events", events);

        model.addAttribute("expedienteId", id);
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
    
        return "expediente/selection";
    }

    @GetMapping("/manage-future-projects/{usuarioId}/{expedienteId}")
    public String gestionarProyectosFuturos(
        @PathVariable Integer usuarioId,
        @PathVariable Integer expedienteId,
        Model model,
        HttpSession session) {
    try {
        Integer idUsuario = (Integer) session.getAttribute("idusuario");
        Optional<usuario> optionalUsuario = usuarioService.findById(idUsuario);
        if (optionalUsuario.isEmpty()) return "redirect:/NoAccess/Access";
        usuario usuario = optionalUsuario.get();

        if (usuario.getPermisos().stream().noneMatch(p -> "EXPEDIENTE_ACCESS".equals(p.getCodigo()))) {
            return "redirect:/NoAccess/Access";
        }

        List<Form> forms;

        // 🔥 Mostrar todos los formularios de ese expediente si es EDGAR o tiene permiso de ver todo
        boolean puedeVerTodos = "EDGAR".equalsIgnoreCase(usuario.getTipo_usuario()) ||
                usuario.getPermisos().stream().anyMatch(p -> "EXPEDIENTE_VIEW_ALL".equals(p.getCodigo()));

        if (puedeVerTodos) {
            forms = formService.findByExpedienteIdAndFuturo(expedienteId, true);
        } else {
            forms = formService.findByUsuarioIdAndExpedienteIdAndFuturo(idUsuario, expedienteId, true);
        }

        model.addAttribute("forms", forms);
        model.addAttribute("expedienteId", expedienteId);
        model.addAttribute("permisos", permisoService.getAllPermisos());
        model.addAttribute("permisosAgrupados", permisoService.getPermisosAgrupadosPorVista());

        usuario.getPermisos().size();
        Set<String> permisos = usuario.getPermisos().stream().map(p -> p.getCodigo()).collect(Collectors.toSet());

        model.addAttribute("EXPEDIENTE_ACCESS", permisos.contains("EXPEDIENTE_ACCESS"));
        model.addAttribute("EXPEDIENTE_CREATE", permisos.contains("EXPEDIENTE_CREATE"));
        model.addAttribute("EXPEDIENTE_EDIT", permisos.contains("EXPEDIENTE_EDIT"));
        model.addAttribute("EXPEDIENTE_DELETE", permisos.contains("EXPEDIENTE_DELETE"));
        model.addAttribute("EXPEDIENTE_FORM_CREATE", permisos.contains("EXPEDIENTE_FORM_CREATE"));
        model.addAttribute("EXPEDIENTE_FORM_PDF", permisos.contains("EXPEDIENTE_FORM_PDF"));
        model.addAttribute("EXPEDIENTE_FORM_EDIT", permisos.contains("EXPEDIENTE_FORM_EDIT"));
        model.addAttribute("EXPEDIENTE_FORM_DELETE", permisos.contains("EXPEDIENTE_FORM_DELETE"));

    } catch (Exception e) {
        model.addAttribute("errorMessage", "Error loading forms: " + e.getMessage());
    }
        // --- CTRL + C ---
        // --- CTRL + V ---

        return "expediente/proyectosFuturos/gestionarProyectosFuturos";
    }

    @GetMapping("/create-future-project")
    public String crearProyectoFuturo(@RequestParam(required = false) Integer expedienteId, @RequestParam(required = false) String fecha_evaluacion, Model model) {
        model.addAttribute("expedienteId", expedienteId);
        model.addAttribute("fechaEvaluacion", fecha_evaluacion);

        return "expediente/proyectosFuturos/crearProyectoFuturo";
    }

    @GetMapping("/view-future-project/{id}")
    public String verProyectoFuturo(@PathVariable Integer id, Model model) {
        try {
            Form form = formService.findById(id).orElse(null);
            if (form == null) {
                model.addAttribute("errorMessage", "Form not found");
                return "redirect:/form/manage";
            }

            model.addAttribute("form", form);

            return "expediente/proyectosFuturos/verProyectoFuturo";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error loading form: " + e.getMessage());
            return "redirect:/form/manage";
        }
    }

    @GetMapping("/edit-future-project/{id}")
    public String editarProyectoFuturo(@PathVariable Integer id, Model model) {
        try {
            Form form = formService.findById(id).orElse(null);
            if (form == null) {
                model.addAttribute("errorMessage", "Form not found");
                return "redirect:/form/manage";
            }

            model.addAttribute("form", form);
            return "expediente/proyectosFuturos/editarProyectoFuturo";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error loading form: " + e.getMessage());
            return "expediente/proyectosFuturos/editarProyectoFuturo";
        }
    }   
}
