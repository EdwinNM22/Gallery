package com.proyecto.galeria.controller;

import com.proyecto.galeria.model.*;
import com.proyecto.galeria.repository.EquipoRepository;
import com.proyecto.galeria.service.*;
import com.proyecto.galeria.service.Impl.UsuarioServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.xhtmlrenderer.pdf.ITextRenderer;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/albumes")
public class AlbumController {

    private final Logger LOGGER = LoggerFactory.getLogger(AlbumController.class);
    @Autowired
    private albumService albumService;
    @Autowired
    UsuarioServiceImpl usuarioService;
    @Autowired
    private subAlbumService subAlbumService;
    @Autowired
    private fotoService fotoService;

    @Autowired
    private Uploadfoto upload;


    @Autowired
    private TemplateEngine templateEngine;

    @Autowired
    private EquipoRepository equipoRepository;
    @Autowired
    private PermisoService permisoService;



    @GetMapping("")
    public String home(Model model) {
        List<album> albumes = albumService.findAll();
        model.addAttribute("albums", albumes);
        List<SubAlbum> subAlbumes = subAlbumService.findAll();
        model.addAttribute("subAlbumes", subAlbumes);



        if (!albumes.isEmpty()) {
            album ultimo = albumes.get(albumes.size() - 1);
            return "redirect:/albumes/show";
        } else {
            return "redirect:/albumes/show";
        }
    }

    @GetMapping("/show")
    public String show(Model model) {
        model.addAttribute("albumes", albumService.findAll());

        return "albumes/show";
    }

    @GetMapping("/create")
    public String albumes(Model model, HttpSession session) {

        // Obtener el ID del usuario de la sesión
        Integer userId = Integer.parseInt(session.getAttribute("idusuario").toString());
        Optional<usuario> optionalUsuario = usuarioService.findById(userId);
        String userRole = optionalUsuario.map(usuario::getTipo_usuario).orElse("USUARIO");


        // Pasar atributos a la vista
        model.addAttribute("albumes", albumService.findAll());
        model.addAttribute("subalbum", subAlbumService.findAll());
        model.addAttribute("userRole", userRole);
        model.addAttribute("permisosAgrupados", permisoService.getPermisosAgrupadosPorVista());
        model.addAttribute("usuarios", usuarioService.findAll());

        //Validar acceso a la vista
        Integer idUsuario = (Integer) session.getAttribute("idusuario");
        Optional<usuario> userOpt = usuarioService.findById(idUsuario);

        if (userOpt.isEmpty() || userOpt.get().getPermisos().stream()
                .noneMatch(p -> "AGENDA_ACCESS".equals(p.getCodigo()))) {
            return "redirect:/NoAccess/Access";
        }


        usuarioService.findById(idUsuario).ifPresentOrElse(user -> {
            user.getPermisos().size(); // Forzar carga

            model.addAttribute("usuarioLogueado", user);

            // Permisos individuales
            Set<String> permisos = user.getPermisos().stream()
                    .map(Permiso::getCodigo)
                    .collect(Collectors.toSet());

            model.addAttribute("AGENDA_DELETE", permisos.contains("AGENDA_DELETE"));
            model.addAttribute("AGENDA_EDIT", permisos.contains("AGENDA_EDIT"));
            model.addAttribute("AGENDA_CREATE", permisos.contains("AGENDA_CREATE"));
            model.addAttribute("AGENDA_PDF", permisos.contains("AGENDA_PDF"));
            model.addAttribute("AGENDA_STATUS", permisos.contains("AGENDA_STATUS"));
            model.addAttribute("AGENDA_NOTES", permisos.contains("AGENDA_NOTES"));
            model.addAttribute("AGENDA_TEAMS", permisos.contains("AGENDA_TEAMS"));

        }, () -> {
            model.addAttribute("AGENDA_DELETE", false);
            model.addAttribute("AGENDA_EDIT", false);
            model.addAttribute("AGENDA_CREATE", false);
            model.addAttribute("AGENDA_PDF", false);
            model.addAttribute("AGENDA_STATUS", false);
            model.addAttribute("AGENDA_NOTES", false);
            model.addAttribute("AGENDA_TEAMS", false);

        });

        return "albumes/create";
    }



    @PostMapping("/save")
    public ResponseEntity<String> save(
            @ModelAttribute album album,
            @RequestParam(required = false) String horaInicioStr,
            @RequestParam(required = false) String horaFinStr,
            @RequestParam(required = false) String notas,
            HttpSession session) {

        LOGGER.info("Saving album: {}", album);

        Object idUsuarioObj = session.getAttribute("idusuario");
        if (idUsuarioObj == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuario no autenticado");
        }

        usuario u = usuarioService.findById(Integer.parseInt(idUsuarioObj.toString()))
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Procesar horaInicio si está presente
        if (horaInicioStr != null && !horaInicioStr.isEmpty()) {
            try {
                LocalTime horaInicio = LocalTime.parse(horaInicioStr);
                album.setHoraInicio(horaInicio);
            } catch (DateTimeParseException e) {
                LOGGER.warn("Formato de hora inválido: {}", horaInicioStr);
            }
        }

        // Procesar horaFin si está presente
        if (horaFinStr != null && !horaFinStr.isEmpty()) {
            try {
                LocalTime horaFin = LocalTime.parse(horaFinStr);
                album.setHoraFin(horaFin);
            } catch (DateTimeParseException e) {
                LOGGER.warn("Formato de hora inválido: {}", horaFinStr);
            }
        } else if (album.getHoraInicio() != null) {
            // Si no hay horaFin pero sí horaInicio, establecer 1 hora después
            album.setHoraFin(album.getHoraInicio().plusHours(1));
        }

        // VALIDACIÓN DE HORAS
        if (album.getHoraInicio() != null && album.getHoraFin() != null) {
            if (!album.getHoraFin().isAfter(album.getHoraInicio())) {
                return ResponseEntity.badRequest().body("La hora de fin debe ser posterior a la hora de inicio");
            }
        }


        // Establecer notas
        album.setNotas(notas);

        album.setUsuario(u);
        album savedAlbum = albumService.save(album);

        SubAlbum subAlbumAntes = new SubAlbum(null, "Antes", "Upload the photos of the initial state of the project.", "Antes", savedAlbum, u);
        SubAlbum subAlbumDespues = new SubAlbum(null, "Después", "Upload the photos of the final state of the project.", "Despues", savedAlbum, u);

        subAlbumService.save(subAlbumAntes);
        subAlbumService.save(subAlbumDespues);

        return ResponseEntity.ok("Álbum guardado con éxito");
    }

    @GetMapping("/{id}")
    public String viewAlbum(@PathVariable Integer id, Model model, HttpSession session) {

        //Validar acceso a la vista
        Integer idUsuario = (Integer) session.getAttribute("idusuario");
        usuarioService.findById(idUsuario).ifPresentOrElse(user -> {
            user.getPermisos().size(); // Forzar carga

            model.addAttribute("usuarioLogueado", user);
            model.addAttribute("usuarios", usuarioService.findAll());
            model.addAttribute("permisosAgrupados", permisoService.getPermisosAgrupadosPorVista());


            // Permisos individuales
            Set<String> permisos = user.getPermisos().stream()
                    .map(Permiso::getCodigo)
                    .collect(Collectors.toSet());

            model.addAttribute("PROYECTOS_CREATE_REPORT", permisos.contains("PROYECTOS_CREATE_REPORT"));
            model.addAttribute("PROYECTOS_CREATE_SECTIONS", permisos.contains("PROYECTOS_CREATE_SECTIONS"));
            model.addAttribute("PROYECTOS_EDIT_SECTIONS", permisos.contains("PROYECTOS_EDIT_SECTIONS"));
            model.addAttribute("PROYECTOS_DELETE_SECTIONS", permisos.contains("PROYECTOS_DELETE_SECTIONS"));

        }, () -> {
            model.addAttribute("PROYECTOS_CREATE_REPORT", false);
            model.addAttribute("PROYECTOS_CREATE_SECTIONS", false);
            model.addAttribute("PROYECTOS_EDIT_SECTIONS", false);
            model.addAttribute("PROYECTOS_DELETE_SECTIONS", false);

        });




        // Obtener el ID del usuario desde la sesión
        Integer userId = Integer.parseInt(session.getAttribute("idusuario").toString());
        // Obtener el usuario desde la base de datos
        Optional<usuario> optionalUsuario = usuarioService.findById(userId);
        // Determinar el rol
        String tipoUsuario = optionalUsuario.map(usuario::getTipo_usuario).orElse("");
        boolean isAdmin = "ADMIN".equals(tipoUsuario);
        model.addAttribute("tipoUsuario", tipoUsuario);

        Optional<album> optionalAlbum = albumService.get(id);
        if (optionalAlbum.isPresent()) {
            album album = optionalAlbum.get();
            List<SubAlbum> subAlbumes = album.getSubAlbumes() != null ? album.getSubAlbumes() : new ArrayList<>();

            // Subálbumes principales
            SubAlbum antes = subAlbumes.stream()
                    .filter(s -> s.getNombre().equals("Antes"))
                    .findFirst()
                    .orElse(null);

            SubAlbum despues = subAlbumes.stream()
                    .filter(s -> s.getNombre().equals("Después"))
                    .findFirst()
                    .orElse(null);

            // Agrupar subálbumes en fragmentos
            Map<String, Fragmento> fragmentosMap = new HashMap<>();

            for (SubAlbum subAlbum : subAlbumes) {
                if (subAlbum.getNombre().contains(" - ")) {
                    String[] parts = subAlbum.getNombre().split(" - ");
                    if (parts.length == 2) {
                        String fragmentName = parts[0];
                        String tipo = parts[1];
                        Fragmento fragmento = fragmentosMap.getOrDefault(fragmentName,
                                new Fragmento(fragmentName, subAlbum.getDescripcion(), new ArrayList<>()));

                        // Actualizar descripción solo si es más específica
                        if (subAlbum.getDescripcion() != null && !subAlbum.getDescripcion().isEmpty()) {
                            fragmento.setDescripcion(subAlbum.getDescripcion());
                        }

                        fragmento.getSubAlbumes().add(subAlbum);
                        fragmentosMap.put(fragmentName, fragmento);
                    }
                }
            }

            List<Fragmento> fragmentos = new ArrayList<>(fragmentosMap.values());

            model.addAttribute("album", album);
            model.addAttribute("subAlbumAntes", antes);
            model.addAttribute("subAlbumDespues", despues);
            model.addAttribute("fragmentos", fragmentos);

            return "albumes/albumes";
        } else {
            return "redirect:/";
        }
    }

    @PostMapping("/saveFragment")
    public String saveFragment(
            @RequestParam Integer albumId,
            @RequestParam(required = false) String fragmentId,
            @RequestParam String name,
            @RequestParam(required = false) String description,
            HttpSession session) {

        try {
            Object idUsuarioObj = session.getAttribute("idusuario");
            if (idUsuarioObj == null) {
                return "redirect:/login";
            }

            usuario u = usuarioService.findById(Integer.parseInt(idUsuarioObj.toString()))
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            Optional<album> optionalAlbum = albumService.get(albumId);
            if (!optionalAlbum.isPresent()) {
                return "redirect:/albumes?error=Album+not+found";
            }

            album album = optionalAlbum.get();

            if ("main".equals(fragmentId)) {
                // Actualizar descripción del fragmento principal
                List<SubAlbum> subAlbumes = album.getSubAlbumes();
                for (SubAlbum subAlbum : subAlbumes) {
                    if ("Antes".equals(subAlbum.getNombre()) || "Después".equals(subAlbum.getNombre())) {
                        subAlbum.setDescripcion(description);
                        subAlbumService.save(subAlbum);
                    }
                }
            } else if (fragmentId == null || fragmentId.isEmpty()) {
                // Crear nuevo fragmento
                SubAlbum subAlbumAntes = new SubAlbum(null, name + " - Antes",
                        description != null ? description : "Fotos del estado inicial del fragmento",
                        "Antes", album, u);

                SubAlbum subAlbumDespues = new SubAlbum(null, name + " - Después",
                        description != null ? description : "Fotos del estado final del fragmento",
                        "Despues", album, u);

                subAlbumService.save(subAlbumAntes);
                subAlbumService.save(subAlbumDespues);
            } else {
                // Editar fragmento existente
                List<SubAlbum> subAlbumes = subAlbumService.findByAlbumId(albumId);
                for (SubAlbum subAlbum : subAlbumes) {
                    if (subAlbum.getNombre().startsWith(fragmentId + " - ")) {
                        String tipo = subAlbum.getNombre().split(" - ")[1];
                        subAlbum.setNombre(name + " - " + tipo);
                        subAlbum.setDescripcion(description);
                        subAlbumService.save(subAlbum);
                    }
                }
            }

            return "redirect:/albumes/" + albumId + "?success=Fragment+saved+successfully";
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/albumes/" + albumId + "?error=Error+saving+fragment";
        }
    }

    @PostMapping("/deleteFragment/{fragmentId}")
    public String deleteFragment(
            @PathVariable String fragmentId,
            @RequestParam Integer albumId) {

        try {
            if ("main".equals(fragmentId)) {
                return "redirect:/albumes/" + albumId + "?error=Cannot+delete+main+fragment";
            }

            List<SubAlbum> subAlbumes = subAlbumService.findByAlbumId(albumId);
            for (SubAlbum subAlbum : subAlbumes) {
                if (subAlbum.getNombre().startsWith(fragmentId + " - ")) {
                    // Eliminar fotos asociadas
                    for (foto foto : subAlbum.getFotos()) {
                        if (!foto.getImagen().equals("default.jpg")) {
                            upload.deleteImage(foto.getImagen());
                        }
                        fotoService.delete(foto.getId());
                    }
                    // Eliminar subálbum
                    subAlbumService.delete(subAlbum.getId());
                }
            }

            return "redirect:/albumes/" + albumId + "?success=Fragment+deleted+successfully";
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/albumes/" + albumId + "?error=Error+deleting+fragment";
        }
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Integer id, Model model, HttpSession session) {
        Optional<album> optionalAlbum = albumService.get(id);

        Integer userId = Integer.parseInt(session.getAttribute("idusuario").toString());
        Optional<usuario> optionalUsuario = usuarioService.findById(userId);

        boolean isEdgar = optionalUsuario.map(user -> "EDGAR".equals(user.getTipo_usuario()))
                .orElse(false);
        model.addAttribute("isEdgar", isEdgar);  // Pasar solo si es EDGAR

        LOGGER.info("search album: {}", optionalAlbum);
        if (optionalAlbum.isPresent()) {
            model.addAttribute("album", optionalAlbum.get());
            return "albumes/edit";
        } else {
            return "redirect:/albumes/create";
        }
    }



    @PostMapping("/update")
    public String update(album album, Model model, HttpSession session) {
        LOGGER.info("Updating album: {}", album);
        albumService.update(album);
        return "redirect:/albumes/create";
    }


    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Integer id, HttpServletRequest request) {
        Optional<album> optionalAlbum = albumService.get(id);

        if (optionalAlbum.isPresent()) {
            album album = optionalAlbum.get();

            for (SubAlbum subAlbum : album.getSubAlbumes()) {
                for (foto foto : subAlbum.getFotos()) {
                    if (!foto.getImagen().equals("default.jpg")) {
                        upload.deleteImage(foto.getImagen());
                    }
                }
                subAlbum.getFotos().clear();
                subAlbumService.save(subAlbum);
            }

            albumService.delete(id);
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Álbum no encontrado");
        }

        // Obtiene la URL de referencia (la página actual)
        String referer = request.getHeader("Referer");
        // Si no hay referencia, redirige a /albumes por defecto
        return "redirect:" + (referer != null ? referer : "/albumes");
    }


    @GetMapping("/{id}/details")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getAlbumDetails(@PathVariable Integer id) {
        Optional<album> optionalAlbum = albumService.get(id);

        if (optionalAlbum.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        album album = optionalAlbum.get();
        Map<String, Object> response = new HashMap<>();

        // Mapear todos los campos necesarios
        response.put("id", album.getId());
        response.put("nombre", album.getNombre());
        response.put("descripcion", album.getDescripcion());
        response.put("fechaInicio", album.getFechaInicio());
        response.put("horaInicio", album.getHoraInicio()); // Nuevo campo
        response.put("horaFin", album.getHoraFin());
        response.put("fechaFin", album.getFechaFin());
        response.put("ubicacion", album.getUbicacion());
        response.put("contacto", album.getContacto());
        response.put("claveAlarma", album.getClaveAlarma());
        response.put("datosAdicionales", album.getDatosAdicionales());
        response.put("notas", album.getNotas()); // Nuevo campo
        response.put("horasPorProyecto", album.getHorasPorProyecto());
        response.put("precioHora", album.getPrecioHora());
        response.put("estado", album.getEstado()); // Nuevo campo

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(response);
    }

    @GetMapping("/calendar-events")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> getCalendarEvents() {
        List<album> albums = albumService.findAll();

        List<Map<String, Object>> events = new ArrayList<>();

        for (album album : albums) {
            Map<String, Object> event = new HashMap<>();
            event.put("id", album.getId());
            event.put("nombre", album.getNombre());
            event.put("descripcion", album.getDescripcion());
            event.put("fechaInicio", album.getFechaInicio());
            event.put("horaInicio", album.getHoraInicio());
            event.put("horaFin", album.getHoraFin());// Nuevo campo
            event.put("fechaFin", album.getFechaFin());
            event.put("ubicacion", album.getUbicacion());
            event.put("notas", album.getNotas()); // Nuevo campo
            event.put("estado", album.getEstado()); // Nuevo campo

            events.add(event);
        }

        return ResponseEntity.ok(events);
    }


    @PostMapping("/change-status/{id}")
    public ResponseEntity<String> changeAlbumStatus(
            @PathVariable Integer id,
            @RequestParam String nuevoEstado) {
        try {
            Optional<album> optionalAlbum = albumService.get(id);
            if (optionalAlbum.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            album album = optionalAlbum.get();

            // Validar que el estado sea uno de los permitidos
            if (!Arrays.asList("pendiente", "completado", "anulado").contains(nuevoEstado)) {
                return ResponseEntity.badRequest().body("Estado no válido");
            }

            album.setEstado(nuevoEstado); // Solo actualiza el estado, sin tocar fechaFin

            albumService.save(album);

            return ResponseEntity.ok("Estado del álbum actualizado a " + nuevoEstado);
        } catch (Exception e) {
            LOGGER.error("Error al cambiar estado del álbum", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al cambiar estado del álbum");
        }
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<InputStreamResource> generatePdf(@PathVariable Integer id) {
        try {
            Optional<album> optionalAlbum = albumService.get(id);
            if (optionalAlbum.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            album album = optionalAlbum.get();

            // Configurar el contexto de Thymeleaf
            Context context = new Context();
            context.setVariable("album", album);

            // Procesar la plantilla HTML
            String htmlContent = templateEngine.process("pdf/detallePDF", context);

            // Configurar el renderizador PDF
            ITextRenderer renderer = new ITextRenderer();
            // Configurar el documento
            renderer.setDocumentFromString(htmlContent);
            renderer.layout();

            // Generar PDF
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            renderer.createPDF(outputStream);
            renderer.finishPDF();

            // Configurar la respuesta
            byte[] pdfBytes = outputStream.toByteArray();
            ByteArrayInputStream inputStream = new ByteArrayInputStream(pdfBytes);

            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Disposition", "attachment; filename=detalle_proyecto_" + album.getId() + ".pdf");

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(new InputStreamResource(inputStream));

        } catch (Exception e) {

            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/update-notes/{id}")
    public ResponseEntity<String> updateNotes(@PathVariable Long id, @RequestParam String notas) {
        try {
            album album = albumService.findById(id.intValue());

            if (album == null) {
                return ResponseEntity.notFound().build();
            }

            album.setNotas(notas);
            albumService.save(album);

            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


    @GetMapping("/{id}/notes")
    @ResponseBody
    public Map<String, String> getAlbumNotes(@PathVariable int id) {
        album album = albumService.findById(id);
        Map<String, String> response = new HashMap<>();
        response.put("notes", album.getNotas());
        response.put("projectName", album.getNombre());
        return response;
    }

    @PostMapping("/{id}/update-notes")
    @ResponseBody
    public ResponseEntity<String> updateAlbumNotes(@PathVariable int id, @RequestBody Map<String, String> request) {
        try {
            String notas = request.get("notes");
            album album = albumService.findById(id);
            album.setNotas(notas);
            albumService.update(album);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error updating notes");
        }
    }

    @GetMapping("/weekly-table/pdf")
    public ResponseEntity<InputStreamResource> generateWeeklyTablePdf() {
        try {
            // Obtener la fecha actual para el título del reporte
            LocalDate now = LocalDate.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM yyyy", new Locale("es", "ES"));
            String monthYear = now.format(formatter);

            // Configurar el contexto de Thymeleaf
            Context context = new Context();
            context.setVariable("monthYear", monthYear);

            // Obtener los eventos del calendario
            List<album> albums = albumService.findAll();
            List<Map<String, Object>> events = new ArrayList<>();

            for (album album : albums) {
                Map<String, Object> event = new HashMap<>();
                event.put("id", album.getId());
                event.put("title", album.getNombre());
                event.put("start", album.getFechaInicio());
                event.put("end", album.getFechaFin() != null ? album.getFechaFin() : album.getFechaInicio());
                event.put("status", album.getEstado());
                event.put("allDay", album.getHoraInicio() == null);
                events.add(event);
            }

            context.setVariable("events", events);

            // Procesar la plantilla HTML
            String htmlContent = templateEngine.process("pdf/weeklyTablePDF", context);

            // Configurar el renderizador PDF
            ITextRenderer renderer = new ITextRenderer();
            renderer.setDocumentFromString(htmlContent);
            renderer.layout();

            // Generar PDF
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            renderer.createPDF(outputStream);
            renderer.finishPDF();

            // Configurar la respuesta
            byte[] pdfBytes = outputStream.toByteArray();
            ByteArrayInputStream inputStream = new ByteArrayInputStream(pdfBytes);

            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Disposition", "attachment; filename=reporte_semanal_" + now + ".pdf");

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(new InputStreamResource(inputStream));

        } catch (Exception e) {
            LOGGER.error("Error al generar PDF de tabla semanal", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // Para los equipos
}
