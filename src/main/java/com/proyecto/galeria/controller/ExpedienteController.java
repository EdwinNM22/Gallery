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

import java.util.List;
import java.util.Map;
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

    @GetMapping("/menu")
    public String menu(HttpSession session, Model model) {
        try {
            Integer idUsuario = (Integer) session.getAttribute("idusuario");
            Optional<usuario> optionalUsuario = usuarioService.findById(idUsuario);
            if (optionalUsuario.isEmpty())
                return "redirect:/NoAccess/Access";

            usuario usuario = optionalUsuario.get();

            model.addAttribute("permisos", permisoService.getAllPermisos());
            model.addAttribute("permisosAgrupados", permisoService.getPermisosAgrupadosPorVista());
            model.addAttribute("usuario", usuario);

            Set<String> permisos = usuario.getPermisos().stream().map(p -> p.getCodigo()).collect(Collectors.toSet());

            model.addAttribute("PROJECT_EVALUATION_ACCESS", permisos.contains("PROJECT_EVALUATION_ACCESS"));
            model.addAttribute("PROJECT_SCHEDULING_ACCESS", permisos.contains("PROJECT_SCHEDULING_ACCESS"));


        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error loading menu: " + e.getMessage());
        }

        return "expediente/representativesMenu";
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

    @GetMapping("/manage-future-projects/{usuarioId}")
    public String gestionarProyectosFuturos(
            @PathVariable Integer usuarioId,
            Model model,
            HttpSession session) {
        try {
            Integer idUsuario = (Integer) session.getAttribute("idusuario");
            Optional<usuario> optionalUsuario = usuarioService.findById(idUsuario);
            if (optionalUsuario.isEmpty())
                return "redirect:/NoAccess/Access";
            usuario usuario = optionalUsuario.get();

            if (usuario.getPermisos().stream().noneMatch(p -> "EXPEDIENTE_ACCESS".equals(p.getCodigo()))) {
                return "redirect:/NoAccess/Access";
            }

            List<Form> forms = formService.findByUsuarioId(idUsuario);

            model.addAttribute("forms", forms);
            model.addAttribute("permisos", permisoService.getAllPermisos());
            model.addAttribute("permisosAgrupados", permisoService.getPermisosAgrupadosPorVista());

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

        return "expediente/proyectosFuturos/gestionarProyectosFuturos";
    }


    @GetMapping("/create-future-project")
    public String crearProyectoFuturo(Model model, HttpSession session,
                                      @RequestParam(value = "success", required = false) String success) {

        Integer idUsuario = (Integer) session.getAttribute("idusuario");
        if (idUsuario != null) {
            usuario user = usuarioService.findById(idUsuario).orElse(null);
            if (user != null) {
                model.addAttribute("nombreEvaluador", user.getNombre());
                model.addAttribute("usuario", user);
            }
        }

        if (success != null) {
            model.addAttribute("success", true);
        }

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
    public String editarProyectoFuturo(@PathVariable Integer id, Model model,
            @RequestParam(value = "success", required = false) String success) {
        try {
            Form form = formService.findById(id).orElse(null);
            if (form == null) {
                model.addAttribute("errorMessage", "Form not found");
                return "redirect:/form/manage";
            }

            if (success != null) {
                model.addAttribute("success", true);
            }

            model.addAttribute("form", form);
            return "expediente/proyectosFuturos/editarProyectoFuturo";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error loading form: " + e.getMessage());
            return "expediente/proyectosFuturos/editarProyectoFuturo";
        }
    }
}
