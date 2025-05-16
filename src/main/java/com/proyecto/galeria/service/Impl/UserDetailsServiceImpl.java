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

/**
 * Custom bridge between Spring-Security and our Usuario entity.
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final IUsuarioService usuarioService;
    private final HttpSession     session;
    private final Logger          log = LoggerFactory.getLogger(UserDetailsServiceImpl.class);

    /* ---------- constructor injection (preferred) ---------- */
    @Autowired
    public UserDetailsServiceImpl(IUsuarioService usuarioService,
                                  HttpSession session) {

        this.usuarioService = usuarioService;
        this.session        = session;
    }

    /* ---------- main contract ---------- */
    @Override
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {

        log.info("Attempting login for: {}", email);

        Optional<usuario> opt = usuarioService.findByEmail(email);
        if (opt.isEmpty()) {
            throw new UsernameNotFoundException("Usuario no encontrado");
        }

        usuario u = opt.get();

        /*  Store a few things we’ll need in views (Thymeleaf) */
        session.setAttribute("idusuario",   u.getId());
        session.setAttribute("tipo_usuario", u.getTipo_usuario());  // ← enables the new buttons

        /*
         *  Spring-Security principal.
         *  Roles here must NOT include the “ROLE_” prefix, because
         *  .roles(…) will add it automatically.
         */
        return User.builder()
                .username(u.getNombre())          // or u.getNombre() if you prefer
                .password(u.getPassword())
                .roles(u.getTipo_usuario())      // e.g. ADMIN / SUPERVISOR
                .build();
    }
}
