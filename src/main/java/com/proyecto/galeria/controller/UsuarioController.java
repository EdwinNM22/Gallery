package com.proyecto.galeria.controller;

import com.proyecto.galeria.model.foto;
import com.proyecto.galeria.model.usuario;
import com.proyecto.galeria.service.IUsuarioService;

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

import static org.hibernate.tool.schema.SchemaToolingLogging.LOGGER;

@Controller
@RequestMapping("/usuario")
public class UsuarioController {

    @Autowired
    private IUsuarioService usuarioService;

    private final Logger logger = LoggerFactory.getLogger(UsuarioController.class);


    BCryptPasswordEncoder passEncode = new BCryptPasswordEncoder();


    @GetMapping("/show")
    public String show(Model model){

        model.addAttribute("usuarios", usuarioService.findAll());
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
    public String create() {
        return "usuario/registro";
    }

    @PostMapping("/save")
    public ResponseEntity<String> save(usuario usuario) {
        logger.info("Usuario registro: {}", usuario);

        // Validar que el tipo de usuario sea válido
        if (!"ADMIN".equals(usuario.getTipo_usuario()) && !"USER".equals(usuario.getTipo_usuario())) {
            return ResponseEntity.badRequest().body("Tipo de usuario no válido");
        }

        usuario.setPassword(passEncode.encode(usuario.getPassword()));
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
                String tipoUsuario = user.get().getTipo_usuario();
                logger.info("Usuario encontrado - Tipo: {}", tipoUsuario);

                if ("ADMIN".equalsIgnoreCase(tipoUsuario)) {
                    return "redirect:/adm";
                } else if ("SUPERVISOR".equalsIgnoreCase(tipoUsuario)) {
                    return "redirect:/supervi";
                } else {
                    return "redirect:/mainMenu";
                }
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
                redirectAttributes.addFlashAttribute("successMessage", "Usuario eliminado correctamente");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Usuario no encontrado");
            }
        } catch (Exception e) {
            logger.error("Error al eliminar usuario: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Error al eliminar usuario");
        }
        return "redirect:/usuario/show";
    }

}
