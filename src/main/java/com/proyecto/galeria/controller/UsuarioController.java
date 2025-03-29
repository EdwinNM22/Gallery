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

    private final Logger logger= LoggerFactory.getLogger(UsuarioController.class);


    BCryptPasswordEncoder passEncode = new BCryptPasswordEncoder();

    @GetMapping("/registro")
    public String create() {
        return "usuario/registro";
    }

    @PostMapping("/save")
    public String save(usuario usuario) {
        logger.info("Usuario registro: {}", usuario);
        usuario.setTipo_usuario("USER");
        usuario.setPassword( passEncode.encode(usuario.getPassword()));
        usuarioService.save(usuario);
        return "redirect:/";
    }

    @GetMapping("/login")
    public String login() {
        return "usuario/login";
    }

    @GetMapping("/acceder")
    public String acceder(usuario usuario, HttpSession session) {
        logger.info("Accesos : {}", usuario);

        Object idUsuarioObj = session.getAttribute("idusuario");
        if (idUsuarioObj == null) {
            logger.info("No hay sesión activa, redirigiendo a /");
            return "redirect:/";
        }

        Optional<usuario> user = usuarioService.findById(Integer.parseInt(idUsuarioObj.toString()));

        if (user.isPresent()) {
            session.setAttribute("idusuario", user.get().getId());
            logger.info("Tipo de usuario: {}", user.get().getTipo_usuario());
            logger.info("ID de usuario desde la sesión: {}", idUsuarioObj);

            if (user.get().getTipo_usuario().equals("ADMIN")) {
                return "redirect:/adm";  // Redirige a /adm si es admin
            } else {
                return "redirect:/";  // Redirige a / si no es admin
            }
        } else {
            logger.info("Usuario no existe");
        }

        return "redirect:/";  // En caso de cualquier error, redirige a /
    }

}



