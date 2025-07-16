package com.proyecto.galeria.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;


@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true) // Habilita @PreAuthorize
public class SpringBootSecurity extends WebSecurityConfigurerAdapter {

    @Autowired
    private UserDetailsService userDetailService;

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailService).passwordEncoder(getEnecoder());
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        String[] publicUrls = { "/css/**", "/js/**", "/equipo/**","/vpsSecurity2024-", "/", "/img/**" };
        String[] userUrls = { "/album/**", "/fotos/**", "/subAlbumes/**", "/home/**" };
        String[] admUrls = { "/adm/**", "/usuario/**", "/supervi/**" };

        http.csrf().disable()
                .authorizeRequests()
                .antMatchers(publicUrls).permitAll()
                .antMatchers(userUrls).hasAnyRole("ADMIN", "SUPERVISOR", "SUPERVISORPLUS", "USER", "EDGAR")
                .antMatchers(admUrls).hasAnyRole("ADMIN", "SUPERVISORPLUS", "EDGAR", "SUPERVISOR", "USER")
                .anyRequest().authenticated()
                .and()
                .formLogin()
                .loginPage("/usuario/login")
                .permitAll()
                .defaultSuccessUrl("/usuario/acceder", true)
                .and()
                .logout()
                .logoutUrl("/cerrar")
                .logoutSuccessUrl("/usuario/login")
                .and()
                .exceptionHandling()
                .accessDeniedPage("/usuario/login");
    }

    @Bean
    public BCryptPasswordEncoder getEnecoder() {
        return new BCryptPasswordEncoder();
    }
}