package com.proyecto.galeria.controller;

import com.proyecto.galeria.model.album;
import com.proyecto.galeria.model.reporte;
import com.proyecto.galeria.model.usuario;
import com.proyecto.galeria.service.ReportePdfService;
import com.proyecto.galeria.service.ReporteService;
import com.proyecto.galeria.service.IUsuarioService;
import com.proyecto.galeria.service.albumService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.List;

@Controller
@RequestMapping("/albumes/{albumId}/reports")
public class ReporteController {

    @Autowired private ReportePdfService pdfService;
    @Autowired private ReporteService servicio;
    @Autowired private IUsuarioService usuarioService;
    @Autowired private albumService albumService;

    private usuario usuarioSesion(HttpSession s) {
        Integer id = Integer.parseInt(s.getAttribute("idusuario").toString());
        return usuarioService.findById(id).orElseThrow();
    }

    @PostMapping("/draft")
    public ResponseEntity<String> draft(@PathVariable Integer albumId,
                                        @RequestParam String contenido,
                                        HttpSession sesion) {
        usuario u = usuarioSesion(sesion);
        servicio.saveDraft(albumId, u.getId(), contenido);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/send")
    public ResponseEntity<String> send(@PathVariable Integer albumId,
                                       @RequestParam String contenido,
                                       HttpSession sesion) {
        usuario u = usuarioSesion(sesion);
        try {
            servicio.send(albumId, u.getId(), contenido);
            return ResponseEntity.ok().build();
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    @GetMapping("")
    public String logs(@PathVariable Integer albumId,
                       Model model,
                       HttpSession sesion) {
        usuario u = usuarioSesion(sesion);
        model.addAttribute("editable", true); // editable siempre en true
        model.addAttribute("reportes", servicio.logs(albumId));
        return "albumes/report_logs :: body";
    }

    @PostMapping("/{id}/edit")
    @ResponseBody
    public String edit(@PathVariable Integer albumId,
                       @PathVariable Integer id,
                       @RequestParam String contenido,
                       HttpSession sesion) {
        usuario u = usuarioSesion(sesion);
        servicio.update(id, contenido);
        return "OK";
    }

    @GetMapping("/pdf")
    public ResponseEntity<byte[]> allPdf(@PathVariable Integer albumId) throws Exception {
        album album = albumService.get(albumId).orElseThrow();
        List<reporte> sent = servicio.logs(albumId);

        String title = "Project reports – " + album.getNombre();
        byte[] pdf   = pdfService.buildPdf(sent, title);

        String fname = safeFileName(album.getNombre()) + "_reports.pdf";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + fname + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> onePdf(@PathVariable Integer albumId,
                                         @PathVariable Integer id) throws Exception {
        album album = albumService.get(albumId).orElseThrow();
        reporte r   = servicio.findSent(id).orElseThrow();

        String title = "Project report – " + album.getNombre();
        byte[] pdf   = pdfService.buildPdf(List.of(r), title);

        String fname = safeFileName(album.getNombre()) + "_report_" + id + ".pdf";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + fname + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    private static String safeFileName(String raw) {
        return raw.replaceAll("[\\\\/:*?\"<>|]", "_")
                .replaceAll("\\s+", "_");
    }

    @PostMapping("/{id}/delete")
    @ResponseBody
    public String delete(@PathVariable Integer albumId,
                         @PathVariable Integer id,
                         HttpSession sesion) {
        usuario u = usuarioSesion(sesion);
        servicio.delete(id);
        return "OK";
    }
}

