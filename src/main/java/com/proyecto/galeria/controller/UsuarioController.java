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

    @PostMapping("/acceder")
    public String acceder(usuario usuario, HttpSession session) {
        Optional<usuario> user = usuarioService.findByEmail(usuario.getEmail());

        if (user.isPresent() && user.get().getPassword().equals(usuario.getPassword())) { // Asegúrate de que la contraseña se verifique
            session.setAttribute("idusuario", user.get().getId());

            if (user.get().getTipo_usuario().equals("ADMIN")) {
                return "redirect:/adm";
            } else {
                return "redirect:/";
            }
        } else {
            // Manejo de error: redirigir a la página de acceso con un mensaje de error
            return "redirect:/"; // Asegúrate de tener una página de login que maneje este parámetro
        }
    }
}

