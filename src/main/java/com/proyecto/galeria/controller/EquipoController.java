package com.proyecto.galeria.controller;

import com.proyecto.galeria.model.*;
import com.proyecto.galeria.repository.EquipoRepository;
import com.proyecto.galeria.service.AdvertenciaTipoService;
import com.proyecto.galeria.service.IUsuarioService;
import com.proyecto.galeria.service.UsuarioAdvertenciaService;
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
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/equipo")
public class EquipoController {

    private final Logger LOGGER = LoggerFactory.getLogger(EquipoController.class);

    private final EquipoRepository equipoRepository;
    private final IUsuarioService usuarioService;
    private final albumService albumService;

    @Autowired
    private UsuarioAdvertenciaService usuarioAdvertenciaService;

    @Autowired
    private AdvertenciaTipoService advertenciaTipoService;


    @Autowired
    public EquipoController(albumService albumService,
                            IUsuarioService usuarioService,
                            EquipoRepository equipoRepository) {
        this.albumService = albumService;
        this.usuarioService = usuarioService;
        this.equipoRepository = equipoRepository;
    }

    @GetMapping("/asignacion")
    public String mostrarAsignacionEquipos(Model model, HttpSession session) {
        Integer userId = (Integer) session.getAttribute("idusuario");
        if (userId == null) return "redirect:/login";

        Optional<usuario> optionalUsuario = usuarioService.findById(userId);
        if (optionalUsuario.isEmpty() ||
                !(optionalUsuario.get().getTipo_usuario().equals("EDGAR") ||
                  optionalUsuario.get().getTipo_usuario().equals("ADMIN"))) {
            return "redirect:/equipo?error=No+autorizado";
        }

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

        if (session.getAttribute("idusuario") == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No autenticado");
        }

        Optional<album> proyectoOpt = albumService.get(proyectoId);
        if (proyectoOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Proyecto no encontrado");
        }

        List<EquipoUsuario> miembros = miembrosIds.stream()
        .map(usuarioService::findById)
        .filter(Optional::isPresent)
        .map(Optional::get)
        .map(this::createTeamMemberWithWarning)
        .collect(Collectors.toList());
    
        if (miembros.isEmpty()) {
            return ResponseEntity.badRequest().body("Se requiere al menos un miembro");
        }

        Equipo equipo = new Equipo();
        equipo.setNombre(nombre);
        equipo.setDescripcion(descripcion);
        equipo.setProyecto(proyectoOpt.get());

        miembros.forEach(eu -> eu.setEquipo(equipo)); // link back
        equipo.setMiembros(miembros);

        equipoRepository.save(equipo);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Equipo creado exitosamente",
                "equipoId", equipo.getId()
        ));
    }

    @PostMapping("/editar")
    @ResponseBody
    public ResponseEntity<?> editarEquipo(
            @RequestParam Integer equipoId,
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String descripcion,
            @RequestParam(required = false) List<Integer> miembrosIds,
            HttpSession session) {
    
        try {
            // Verify session
            if (session.getAttribute("idusuario") == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("success", false, "message", "No autenticado"));
            }
    
            Optional<Equipo> equipoOpt = equipoRepository.findById(equipoId);
            if (equipoOpt.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "Equipo no encontrado"));
            }
    
            Equipo equipo = equipoOpt.get();
    
            // Update basic fields if provided
            if (nombre != null) equipo.setNombre(nombre);
            if (descripcion != null) equipo.setDescripcion(descripcion);
    
            // Handle miembros updates if provided
            if (miembrosIds != null) {
                Set<Integer> currentMemberIds = equipo.getMiembros().stream()
                        .map(eu -> eu.getUsuario().getId())
                        .collect(Collectors.toSet());

                // Remove existing members not in new list
                List<EquipoUsuario> toRemove = equipo.getMiembros().stream()
                        .filter(eu -> !miembrosIds.contains(eu.getUsuario().getId()))
                        .collect(Collectors.toList());
                toRemove.forEach(miembro -> equipo.removeMiembro(miembro));

                // Add new members with warnings
                for (Integer userId : miembrosIds) {
                    if (!currentMemberIds.contains(userId)) {
                        usuarioService.findById(userId).ifPresent(usuario -> {
                            EquipoUsuario nuevoMiembro = createTeamMemberWithWarning(usuario);
                            nuevoMiembro.setEquipo(equipo);
                            equipo.getMiembros().add(nuevoMiembro);
                        });
                    }
                }
            }
            
            equipoRepository.save(equipo);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Equipo actualizado exitosamente"
            ));
    
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of(
                        "success", false,
                        "message", "Error al actualizar equipo: " + e.getMessage()
                    ));
        }
    }

    @PostMapping("/eliminar/{id}")
    @ResponseBody
    public ResponseEntity<?> eliminarEquipo(@PathVariable Integer id, HttpSession session) {
        if (session.getAttribute("idusuario") == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No autenticado");
        }
    
        Optional<Equipo> equipoOpt = equipoRepository.findById(id);
        if (equipoOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Equipo no encontrado");
        }
    
        Equipo equipo = equipoOpt.get();
    
        // Eliminar manualmente las advertencias asociadas
        equipo.getMiembros().forEach(eu -> {
            UsuarioAdvertencia ua = eu.getUsuarioAdvertencia();
            if (ua != null) {
                try {
                    usuarioAdvertenciaService.delete(ua.getId());
                } catch (Exception e) {
                    LOGGER.warn("No se pudo eliminar la advertencia del usuario con ID: {}", eu.getUsuario().getId(), e);
                }
            }
        });
    
        equipoRepository.delete(equipo);
    
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Equipo eliminados exitosamente"
        ));
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
                .map(eu -> eu.getUsuario().getId())
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

                    List<Map<String, Object>> miembros = equipo.getMiembros().stream()
                            .map(eu -> {
                                usuario m = eu.getUsuario();
                                Map<String, Object> miembroMap = new HashMap<>();
                                miembroMap.put("id", m.getId());
                                miembroMap.put("nombre", m.getNombre());
                                miembroMap.put("email", m.getEmail());
                                miembroMap.put("rol", m.getTipo_usuario());
                                miembroMap.put("estado", eu.getEstado());
                                return miembroMap;
                            })
                            .collect(Collectors.toList());

                    equipoMap.put("miembros", miembros);
                    return equipoMap;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

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

    @PostMapping("/confirmar-miembro")
    @ResponseBody
    public ResponseEntity<?> confirmarMiembro(
            @RequestParam Integer equipoId,
            @RequestParam Integer usuarioId,
            HttpSession session) {

        // Verificar sesión
        if (session.getAttribute("idusuario") == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No autenticado");
        }

        Optional<Equipo> equipoOpt = equipoRepository.findById(equipoId);
        if (equipoOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Equipo no encontrado");
        }

        Equipo equipo = equipoOpt.get();

        // Buscar el EquipoUsuario correspondiente
        Optional<EquipoUsuario> euOpt = equipo.getMiembros().stream()
                .filter(eu -> eu.getUsuario().getId().equals(usuarioId))
                .findFirst();

        if (euOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Miembro no encontrado en el equipo");
        }

        EquipoUsuario eu = euOpt.get();
        eu.setEstado("confirmado");

        // Eliminar advertencia si existe
        UsuarioAdvertencia advertencia = eu.getUsuarioAdvertencia();
        if (advertencia != null) {
            usuarioAdvertenciaService.delete(advertencia.getId());
            eu.setUsuarioAdvertencia(null); // prevenir referencias inválidas
        }

        // Persistir cambios
        equipoRepository.save(equipo);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Succesfully confirmed participation"
        ));
    }

    @PostMapping("/rechazar-miembro")
    @ResponseBody
    public ResponseEntity<?> rechazarMiembro(
            @RequestParam Integer equipoId,
            @RequestParam Integer usuarioId,
            HttpSession session) {

        // Verificar sesión
        if (session.getAttribute("idusuario") == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No autenticado");
        }

        Optional<Equipo> equipoOpt = equipoRepository.findById(equipoId);
        if (equipoOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Equipo no encontrado");
        }

        Equipo equipo = equipoOpt.get();

        // Buscar el EquipoUsuario correspondiente
        Optional<EquipoUsuario> euOpt = equipo.getMiembros().stream()
                .filter(eu -> eu.getUsuario().getId().equals(usuarioId))
                .findFirst();

        if (euOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Miembro no encontrado en el equipo");
        }

        EquipoUsuario eu = euOpt.get();
        eu.setEstado("rechazado");

        // Eliminar advertencia si existe
        UsuarioAdvertencia advertencia = eu.getUsuarioAdvertencia();
        if (advertencia != null) {
            usuarioAdvertenciaService.delete(advertencia.getId());
        }

        equipoRepository.save(equipo);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Succesfully rejected member"
        ));
    }

    private EquipoUsuario createTeamMemberWithWarning(usuario usuario) {
        EquipoUsuario eu = new EquipoUsuario();
        eu.setUsuario(usuario);
        eu.setEstado("pendiente");
        
        // Create warning
        AdvertenciaTipo advertencia = advertenciaTipoService.findByName("equipo_confirmar")
            .stream()
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Advertencia tipo 'equipo_confirmar' no encontrada"));
        
        UsuarioAdvertencia ua = new UsuarioAdvertencia();
        ua.setUsuario(usuario);
        ua.setAdvertenciaTipo(advertencia);
        usuarioAdvertenciaService.save(ua);
        
        eu.setUsuarioAdvertencia(ua);
        return eu;
    }
}
