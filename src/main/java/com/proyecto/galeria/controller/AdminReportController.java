package com.proyecto.galeria.controller;


import com.proyecto.galeria.model.Permiso;
import com.proyecto.galeria.model.reporte;
import com.proyecto.galeria.model.usuario;
import com.proyecto.galeria.service.IUsuarioService;
import com.proyecto.galeria.service.PermisoService;
import com.proyecto.galeria.service.ReporteService;
import com.proyecto.galeria.service.ReportePdfService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;


@Controller
@RequestMapping("/admin/reports")
public class AdminReportController {

    @Autowired private ReporteService   reporteService;
    @Autowired private ReportePdfService reportePdfService;
    @Autowired private IUsuarioService usuarioService;
    @Autowired private PermisoService permisoService;

    /* ------------- list view ------------- */
    @GetMapping("")
    public String list(Model model, HttpSession session) {
        List<reporte> lista = reporteService.allSent();

        model.addAttribute("reportes", lista);
        model.addAttribute("usuarios", usuarioService.findAll());
        model.addAttribute("permisosAgrupados", permisoService.getPermisosAgrupadosPorVista());

        // para dar permisos a edgar
        Integer userId = Integer.parseInt(session.getAttribute("idusuario").toString());
        // Buscar el usuario y obtener su rol
        Optional<usuario> optionalUsuario = usuarioService.findById(userId);
        String userRole = optionalUsuario.map(usuario::getTipo_usuario).orElse("USUARIO");
        model.addAttribute("userRole", userRole);




        //Validar acceso a la vista
        Integer idUsuario = (Integer) session.getAttribute("idusuario");
        Optional<usuario> userOpt = usuarioService.findById(idUsuario);

        if (userOpt.isEmpty() || userOpt.get().getPermisos().stream()
                .noneMatch(p -> "REPORTES_ACCESS".equals(p.getCodigo()))) {
            return "redirect:/NoAccess/Access";
        }

        usuarioService.findById(idUsuario).ifPresentOrElse(user -> {
            user.getPermisos().size(); // Forzar carga

            model.addAttribute("usuarioLogueado", user);

            // Permisos individuales
            Set<String> permisos = user.getPermisos().stream()
                    .map(Permiso::getCodigo)
                    .collect(Collectors.toSet());

            model.addAttribute("REPORTES_EDIT", permisos.contains("REPORTES_EDIT"));
            model.addAttribute("REPORTES_DELETE", permisos.contains("REPORTES_DELETE"));
            model.addAttribute("REPORTES_PDF", permisos.contains("REPORTE_PDF"));


        }, () -> {
            model.addAttribute("REPORTES_EDIT", false);
            model.addAttribute("REPORTES_DELETE", false);
            model.addAttribute("REPORTE_PDF", false);

        });


        return "adm/reports";
    }

    /* ------------- edit (inline) --------- */
    @PostMapping("/{id}")
    @ResponseBody
    public String update(@PathVariable Integer id,
                         @RequestParam String contenido) {

        reporteService.update(id, contenido);
        return "OK";
    }

    /* ------------- download single ------- */
    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> singlePdf(@PathVariable Integer id) throws Exception {

        reporte r = reporteService.findSent(id).orElseThrow();
        byte[] bytes = reportePdfService.buildPdf(List.of(r), "Project report – " +
                r.getAlbum().getNombre());

        String fname = safeFileName(r.getAlbum().getNombre()) + "_report_" + id + ".pdf";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + fname + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(bytes);
    }

    private static String safeFileName(String raw) {
        return raw.replaceAll("[\\\\/:*?\"<>|]", "_")
                .replaceAll("\\s+", "_");
    }
}
