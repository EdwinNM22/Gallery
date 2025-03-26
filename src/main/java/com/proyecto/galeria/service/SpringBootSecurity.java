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
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;

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
        String[] userUrls = {"/album/**", "/fotos/**"};
        String[] admUrls =  {"/adm/**"};

        http.csrf().disable()
                .authorizeRequests()
                .antMatchers(publicUrls).permitAll() // Permitir login y archivos estáticos
                .antMatchers(userUrls).hasRole("USER") // Solo acceso a usuario con rol USER
                .antMatchers(admUrls).hasRole("ADMIN")
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
