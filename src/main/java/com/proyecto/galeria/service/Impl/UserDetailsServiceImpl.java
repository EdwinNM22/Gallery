package com.proyecto.galeria.service.Impl;

import com.proyecto.galeria.model.usuario;
import com.proyecto.galeria.service.IUsuarioService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;
import java.util.Optional;


@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final IUsuarioService usuarioService;
    private final HttpSession     session;
    private final Logger          log = LoggerFactory.getLogger(UserDetailsServiceImpl.class);

    @Autowired
    public UserDetailsServiceImpl(IUsuarioService usuarioService,
                                  HttpSession session) {
        this.usuarioService = usuarioService;
        this.session        = session;
    }

    @Override
    public UserDetails loadUserByUsername(String input)
            throws UsernameNotFoundException {

        log.info("Attempting login for: {}", input);

        Optional<usuario> opt = usuarioService.findBynombre(input);

        if (opt.isEmpty()) {
            opt = usuarioService.findByEmail(input);
            if (opt.isEmpty()) {
                throw new UsernameNotFoundException("Usuario no encontrado");
            }
        }

        usuario u = opt.get();

        session.setAttribute("idusuario",   u.getId());
        session.setAttribute("tipo_usuario", u.getTipo_usuario());

        return User.builder()
                .username(u.getNombre())
                .password(u.getPassword())
                .roles(u.getTipo_usuario())
                .build();
    }
}
