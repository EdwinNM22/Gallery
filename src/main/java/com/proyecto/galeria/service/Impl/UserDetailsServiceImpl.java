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

    @Autowired
    private IUsuarioService usuarioService;


    @Autowired
    HttpSession session;


    private Logger log = LoggerFactory.getLogger(UserDetailsServiceImpl.class);


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("Este es el username");
        Optional<usuario> optionalUser=usuarioService.findByEmail(username);
        if (optionalUser.isPresent()) {
            log.info("Esto es el id del usuario: {}", optionalUser.get().getId());
            session.setAttribute("idusuario", optionalUser.get().getId());
            usuario usuario= optionalUser.get();
            return User.builder().username(usuario.getNombre()).password(usuario.getPassword()).roles(usuario.getTipo_usuario()).build();
        }else {
            throw new UsernameNotFoundException("Usuario no encontrado");
        }
    }
}
