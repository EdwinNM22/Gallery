package com.proyecto.galeria.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;


@Configuration
@EnableWebSecurity
public class SpringBootSecurity extends WebSecurityConfigurerAdapter {

    @Autowired
    private UserDetailsService userDetailService;


    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailService).passwordEncoder(getEnecoder());
    }


    @Override
    protected void configure(HttpSecurity http) throws Exception {
        String[] publicUrls = {"/usuario/**", "/css/**", "/js/**"};
        String[] userUrls = {"/album/**", "/fotos/**", "/subAlbumes/**"};
        String[] admUrls = {"/adm/**"};

        http.csrf().disable()
                .authorizeRequests()
                .antMatchers(publicUrls).permitAll() // Permitir login y archivos estáticos
                .antMatchers(userUrls).hasAnyRole("USER", "ADMIN", "SUPERVISOR") // Acceso a usuarios y administradores
                .antMatchers(admUrls).hasRole("ADMIN") // Solo administradores pueden acceder
                .anyRequest().authenticated() // Bloquear acceso a no autenticados
                .and()
                .formLogin()
                .loginPage("/usuario/login") // Página de login
                .permitAll()
                .defaultSuccessUrl("/usuario/acceder", true) // Redirigir tras login exitoso
                .and()
                .logout()
                .logoutUrl("/cerrar") // URL para cerrar sesión
                .logoutSuccessUrl("/usuario/login") // Redirigir tras logout
                .and()
                .exceptionHandling()
                .accessDeniedPage("/usuario/login"); // Redirigir si no tiene permisos
    }
    @Bean
    public BCryptPasswordEncoder getEnecoder() {
        return new BCryptPasswordEncoder();
    }

}