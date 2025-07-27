package com.proyecto.galeria.controller;

import com.proyecto.galeria.model.Permiso;
import com.proyecto.galeria.model.foto;
import com.proyecto.galeria.model.usuario;
import com.proyecto.galeria.service.IUsuarioService;

import com.proyecto.galeria.service.PermisoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.hibernate.tool.schema.SchemaToolingLogging.LOGGER;

@Controller
@RequestMapping("/usuario")
public class UsuarioController {

    @Autowired
    private IUsuarioService usuarioService;

    @Autowired
    private PermisoService permisoService;

    private final Logger logger = LoggerFactory.getLogger(UsuarioController.class);


    BCryptPasswordEncoder passEncode = new BCryptPasswordEncoder();


    @GetMapping("/show")
    public String show(Model model, HttpSession session) {

        model.addAttribute("usuarios", usuarioService.findAll());
        model.addAttribute("permisos", permisoService.getAllPermisos());
        model.addAttribute("permisosAgrupados", permisoService.getPermisosAgrupadosPorVista());


        //Validar acceso a la vista
        Integer idUsuario = (Integer) session.getAttribute("idusuario");
        Optional<usuario> userOpt = usuarioService.findById(idUsuario);



        // para dar permisos a edgar
        Integer userId = Integer.parseInt(session.getAttribute("idusuario").toString());
        // Buscar el usuario y obtener su rol
        Optional<usuario> optionalUsuario = usuarioService.findById(userId);
        String userRole = optionalUsuario.map(usuario::getTipo_usuario).orElse("USUARIO");
        model.addAttribute("userRole", userRole);

        if (userOpt.isEmpty() || userOpt.get().getPermisos().stream()
                .noneMatch(p -> "USUARIOS_ACCESS".equals(p.getCodigo()))) {
            return "redirect:/NoAccess/Access";
        }

        usuarioService.findById(idUsuario).ifPresentOrElse(user -> {
            user.getPermisos().size(); // Forzar carga

            model.addAttribute("usuarioLogueado", user);

            // Permisos individuales
            Set<String> permisos = user.getPermisos().stream()
                    .map(Permiso::getCodigo)
                    .collect(Collectors.toSet());

            model.addAttribute("USUARIOS_EDIT_PASSWORD", permisos.contains("USUARIOS_EDIT_PASSWORD"));
            model.addAttribute("USUARIOS_DELETE", permisos.contains("USUARIOS_DELETE"));


        }, () -> {
            model.addAttribute("USUARIOS_EDIT_PASSWORD", false);
            model.addAttribute("USUARIOS_DELETE", false);


        });

        return "usuario/show";
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Integer id, Model model) {
        usuario usuario = new usuario();
        Optional<usuario> usuarioOpt = usuarioService.get(id);
        usuario = usuarioOpt.get();

        model.addAttribute("usuario", usuario);
        return "usuario/edit";
    }

    @PostMapping("/update")
    public String update(usuario usuarioParcial) {
        // Buscar el usuario existente por ID
        Optional<usuario> usuarioOpt = usuarioService.get(usuarioParcial.getId());
        if (usuarioOpt.isPresent()) {
            usuario usuarioExistente = usuarioOpt.get();

            // Actualizar solo los campos modificables
            usuarioExistente.setNombre(usuarioParcial.getNombre());
            usuarioExistente.setTipo_usuario(usuarioParcial.getTipo_usuario());

            // Guardar el usuario actualizado
            usuarioService.save(usuarioExistente);
        }
        return "redirect:/usuario/show";
    }

    @PostMapping("/changeUserType")
    public String changeUserType(@RequestParam Integer id, @RequestParam String newType) {
        Optional<usuario> usuarioOpt = usuarioService.get(id);
        if (usuarioOpt.isPresent()) {
            usuario usuarioExistente = usuarioOpt.get();
            usuarioExistente.setTipo_usuario(newType);
            usuarioService.save(usuarioExistente);
        }
        return "redirect:/usuario/show";
    }

    @GetMapping("/vpsSecurity2024-")
    public String create(HttpSession session) {

        //Validar acceso a la vista
        Integer idUsuario = (Integer) session.getAttribute("idusuario");
        Optional<usuario> userOpt = usuarioService.findById(idUsuario);

        if (userOpt.isEmpty() || userOpt.get().getPermisos().stream()
                .noneMatch(p -> "USUARIOS_CREATE".equals(p.getCodigo()))) {
            return "redirect:/NoAccess/Access";
        }
        return "usuario/registro";
    }

    @PostMapping("/save")
    public ResponseEntity<String> save(usuario usuario) {
        logger.info("Usuario registro: {}", usuario);

        // Validar que el tipo de usuario sea válido
        if (!"ADMIN".equals(usuario.getTipo_usuario()) &&
                !"USER".equals(usuario.getTipo_usuario()) &&
                !"SUPERVISOR".equals(usuario.getTipo_usuario()) &&
                !"SUPERVISORPLUS".equals(usuario.getTipo_usuario())) {
            return ResponseEntity.badRequest().body("Tipo de usuario no válido");
        }

        // Codificar la contraseña antes de guardarla
        usuario.setPassword(passEncode.encode(usuario.getPassword()));

        // Guardar el usuario
        usuarioService.save(usuario);
        return ResponseEntity.ok("Usuario registrado");
    }


    @GetMapping("/login")
    public String login() {
        return "usuario/login";
    }

    @GetMapping("/acceder")
    public String acceder(HttpSession session) {
        try {
            Object idUsuarioObj = session.getAttribute("idusuario");
            logger.info("Intento de acceso - ID en sesión: {}", idUsuarioObj);

            if (idUsuarioObj == null) {
                logger.warn("No hay sesión activa - Redirigiendo a login");
                return "redirect:/usuario/login";
            }

            Optional<usuario> user = usuarioService.findById(Integer.parseInt(idUsuarioObj.toString()));

            if (user.isPresent()) {
                logger.info("Usuario encontrado - Redirigiendo a /adm");
                return "redirect:/adm";
            } else {
                logger.error("Usuario no encontrado en BD pero existe en sesión");
                session.invalidate();
                return "redirect:/usuario/login";
            }

        } catch (Exception e) {
            logger.error("Error en el acceso: {}", e.getMessage());
            return "redirect:/usuario/login";
        }
    }



    @PostMapping("/delete")
    public String deleteUser(@RequestParam Integer id, RedirectAttributes redirectAttributes) {
        try {
            Optional<usuario> usuarioOpt = usuarioService.get(id);
            if (usuarioOpt.isPresent()) {
                usuarioService.delete(id);
                redirectAttributes.addFlashAttribute("successMessage", "User succesfully deleted");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "User not found");
            }
        } catch (Exception e) {
            logger.error("Error al eliminar usuario: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Error al eliminar usuario");
        }
        return "redirect:/usuario/show";
    }

    @PostMapping("/changePassword")
    public String changePassword(
            @RequestParam Integer id,
            @RequestParam String newPassword,
            @RequestParam String confirmPassword,
            RedirectAttributes redirectAttributes) {

        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Las contraseñas no coinciden");
            return "redirect:/usuario/show";
        }
        Optional<usuario> usuarioOpt = usuarioService.get(id);
        if (usuarioOpt.isPresent()) {
            usuario usuarioExistente = usuarioOpt.get();
            usuarioExistente.setPasswordSinEncriptar(newPassword);
            usuarioExistente.setPassword(passEncode.encode(newPassword));
            usuarioService.save(usuarioExistente);
            redirectAttributes.addFlashAttribute("successMessage", "Contraseña cambiada exitosamente");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Usuario no encontrado");
        }

        return "redirect:/usuario/show";
    }


}
