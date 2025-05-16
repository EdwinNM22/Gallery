package com.proyecto.galeria.controller;

import com.proyecto.galeria.model.Equipo;
import com.proyecto.galeria.model.album;
import com.proyecto.galeria.model.usuario;
import com.proyecto.galeria.repository.EquipoRepository;
import com.proyecto.galeria.service.IUsuarioService;
import com.proyecto.galeria.service.Impl.UsuarioServiceImpl;
import com.proyecto.galeria.service.albumService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/equipo")
public class EquipoController {

    private final Logger LOGGER = LoggerFactory.getLogger(EquipoController.class);

    @Autowired
    private EquipoRepository equipoRepository;
    @Autowired
    private IUsuarioService usuarioService;
    @Autowired
    private albumService albumService;

    @Autowired
    private UsuarioServiceImpl usuarioServiceImpl;

    @Autowired
    public EquipoController(albumService albumService,
                            UsuarioServiceImpl usuarioService,
                            EquipoRepository equipoRepository) {
        this.albumService = albumService;
        this.usuarioService = usuarioService;
        this.equipoRepository = equipoRepository;
    }

    @GetMapping("/asignacion")
    public String mostrarAsignacionEquipos(Model model, HttpSession session) {
        // Verificar permisos
        Integer userId = (Integer) session.getAttribute("idusuario");
        if (userId == null) {
            return "redirect:/login";
        }

        Optional<usuario> optionalUsuario = usuarioService.findById(userId);
        if (optionalUsuario.isEmpty() ||
                !(optionalUsuario.get().getTipo_usuario().equals("EDGAR") ||
                        optionalUsuario.get().getTipo_usuario().equals("ADMIN"))) {
            return "redirect:/equipo?error=No+autorizado";
        }

        // Obtener datos
        List<usuario> empleados = usuarioService.findAll().stream()
                .filter(u -> !u.getTipo_usuario().equals("ADMIN"))
                .collect(Collectors.toList());

        List<album> proyectos = albumService.findAll().stream()
                .filter(a -> a.getEstado().equals("pendiente") || a.getEstado().equals("en_progreso"))
                .collect(Collectors.toList());

        List<Equipo> equipos = equipoRepository.findAll();

        model.addAttribute("empleados", empleados);
        model.addAttribute("proyectos", proyectos);
        model.addAttribute("equipos", equipos);
        model.addAttribute("usuarioSession", optionalUsuario.get());

        return "equipo/asignacion";
    }

    @PostMapping("/guardar")
    @ResponseBody
    public ResponseEntity<?> guardarEquipo(
            @RequestParam String nombre,
            @RequestParam String descripcion,
            @RequestParam Integer proyectoId,
            @RequestParam List<Integer> miembrosIds,
            HttpSession session) {

        try {
            if (session.getAttribute("idusuario") == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No autenticado");
            }

            Optional<album> proyectoOpt = albumService.get(proyectoId);
            if (proyectoOpt.isEmpty()) {
                return ResponseEntity.badRequest().body("Proyecto no encontrado");
            }

            List<usuario> miembros = miembrosIds.stream()
                    .map(usuarioService::findById)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toList());

            if (miembros.isEmpty()) {
                return ResponseEntity.badRequest().body("Se requiere al menos un miembro");
            }

            Equipo equipo = new Equipo();
            equipo.setNombre(nombre);
            equipo.setDescripcion(descripcion);
            equipo.setProyecto(proyectoOpt.get());
            equipo.setMiembros(miembros);

            equipoRepository.save(equipo);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Equipo creado exitosamente",
                    "equipoId", equipo.getId()
            ));
        } catch (Exception e) {
            LOGGER.error("Error al guardar equipo", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al guardar: " + e.getMessage()));
        }
    }

    @PostMapping("/editar")
    @ResponseBody
    public ResponseEntity<?> editarEquipo(
            @RequestParam Integer equipoId,
            @RequestParam String nombre,
            @RequestParam String descripcion,
            @RequestParam Integer proyectoId,
            @RequestParam List<Integer> miembrosIds,
            HttpSession session) {

        try {
            // Validación de sesión
            if (session.getAttribute("idusuario") == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No autenticado");
            }

            // Validar que el equipo existe
            Optional<Equipo> equipoOpt = equipoRepository.findById(equipoId);
            if (equipoOpt.isEmpty()) {
                return ResponseEntity.badRequest().body("Equipo no encontrado");
            }

            // Validar proyecto
            Optional<album> proyectoOpt = albumService.get(proyectoId);
            if (proyectoOpt.isEmpty()) {
                return ResponseEntity.badRequest().body("Proyecto no encontrado");
            }

            // Validar miembros
            List<usuario> miembros = miembrosIds.stream()
                    .map(usuarioService::findById)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toList());

            if (miembros.isEmpty()) {
                return ResponseEntity.badRequest().body("Se requiere al menos un miembro");
            }

            // Actualizar equipo
            Equipo equipo = equipoOpt.get();
            equipo.setNombre(nombre);
            equipo.setDescripcion(descripcion);
            equipo.setProyecto(proyectoOpt.get());
            equipo.setMiembros(miembros);

            equipoRepository.save(equipo);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Equipo actualizado exitosamente",
                    "equipoId", equipo.getId()
            ));
        } catch (Exception e) {
            LOGGER.error("Error al actualizar equipo", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al actualizar: " + e.getMessage()));
        }
    }

    @PostMapping("/eliminar/{id}")
    @ResponseBody
    public ResponseEntity<?> eliminarEquipo(@PathVariable Integer id, HttpSession session) {
        try {
            // Validación de sesión
            if (session.getAttribute("idusuario") == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No autenticado");
            }

            // Verificar que el equipo existe
            if (!equipoRepository.existsById(id)) {
                return ResponseEntity.badRequest().body("Equipo no encontrado");
            }

            // Eliminar el equipo
            equipoRepository.deleteById(id);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Equipo eliminado exitosamente"
            ));
        } catch (Exception e) {
            LOGGER.error("Error al eliminar equipo", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al eliminar: " + e.getMessage()));
        }
    }

    @GetMapping("/detalles/{id}")
    @ResponseBody
    public ResponseEntity<?> obtenerDetallesEquipo(@PathVariable Integer id) {
        Optional<Equipo> equipoOpt = equipoRepository.findById(id);
        if (equipoOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Equipo equipo = equipoOpt.get();
        Map<String, Object> response = new HashMap<>();
        response.put("id", equipo.getId());
        response.put("nombre", equipo.getNombre());
        response.put("descripcion", equipo.getDescripcion());
        response.put("proyectoId", equipo.getProyecto().getId());
        response.put("miembrosIds", equipo.getMiembros().stream()
                .map(usuario::getId)
                .collect(Collectors.toList()));

        return ResponseEntity.ok(response);
    }

    @GetMapping("/por-proyecto/{proyectoId}")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> obtenerEquiposPorProyecto(
            @PathVariable Integer proyectoId) {

        List<Equipo> equipos = equipoRepository.findByProyectoId(proyectoId);

        List<Map<String, Object>> response = equipos.stream()
                .map(equipo -> {
                    Map<String, Object> equipoMap = new HashMap<>();
                    equipoMap.put("id", equipo.getId());
                    equipoMap.put("nombre", equipo.getNombre());
                    equipoMap.put("descripcion", equipo.getDescripcion());
                    equipoMap.put("miembros", equipo.getMiembros().stream()
                            .map(m -> Map.of(
                                    "id", m.getId(),
                                    "nombre", m.getNombre(),
                                    "email", m.getEmail(),
                                    "rol", m.getTipo_usuario()
                            ))
                            .collect(Collectors.toList()));
                    return equipoMap;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    // lista de trabajadores para el calendario

    @GetMapping("/empleados/json")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> getEmpleadosJson() {
        List<usuario> empleados = usuarioService.findAll().stream()
                .filter(u -> !u.getTipo_usuario().equals("ADMIN"))
                .collect(Collectors.toList());

        List<Map<String, Object>> response = empleados.stream()
                .map(e -> {
                    Map<String, Object> emp = new HashMap<>();
                    emp.put("id", e.getId());
                    emp.put("nombre", e.getNombre());
                    emp.put("email", e.getEmail());
                    emp.put("tipo_usuario", e.getTipo_usuario());
                    return emp;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }
}