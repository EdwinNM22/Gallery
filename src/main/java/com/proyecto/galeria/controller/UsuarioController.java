package com.proyecto.galeria.controller;

import com.proyecto.galeria.model.usuario;
import com.proyecto.galeria.service.IUsuarioService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpSession;
import java.util.Optional;

@Controller
@RequestMapping("/usuario")
public class UsuarioController {

    @Autowired
    private IUsuarioService usuarioService;

    private final Logger logger = LoggerFactory.getLogger(UsuarioController.class);

    BCryptPasswordEncoder passEncode = new BCryptPasswordEncoder();

    @GetMapping("/registro")
    public String create() {
        return "usuario/registro";
    }

    @PostMapping("/save")
    public String save(usuario usuario) {
        logger.info("Usuario registro: {}", usuario);
        usuario.setTipo_usuario("USER");
        usuario.setPassword(passEncode.encode(usuario.getPassword()));
        usuarioService.save(usuario);
        return "redirect:/";
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
}
