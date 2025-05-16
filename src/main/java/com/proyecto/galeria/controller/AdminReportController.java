package com.proyecto.galeria.controller;

import com.proyecto.galeria.model.reporte;
import com.proyecto.galeria.service.ReporteService;
import com.proyecto.galeria.service.ReportePdfService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin/reports")
public class AdminReportController {

    @Autowired private ReporteService   servicio;
    @Autowired private ReportePdfService pdf;

    /* ------------- list view ------------- */
    @GetMapping("")
    public String list(Model model) {
        List<reporte> lista = servicio.allSent();
        model.addAttribute("reportes", lista);
        return "adm/reports";
    }

    /* ------------- edit (inline) --------- */
    @PostMapping("/{id}")
    @ResponseBody
    public String update(@PathVariable Integer id,
                         @RequestParam String contenido) {

        servicio.update(id, contenido);
        return "OK";
    }

    /* ------------- download single ------- */
    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> singlePdf(@PathVariable Integer id) throws Exception {

        reporte r = servicio.findSent(id).orElseThrow();
        byte[] bytes = pdf.buildPdf(List.of(r), "Project report – " +
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
