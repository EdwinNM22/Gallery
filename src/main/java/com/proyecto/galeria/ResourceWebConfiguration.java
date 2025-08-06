package com.proyecto.galeria;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class ResourceWebConfiguration implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/planos/**")
                .addResourceLocations("file:/opt/Gallery/planos/"); // usado por PlanosController

        registry.addResourceHandler("/images/**")
                .addResourceLocations("file:/opt/Gallery/images/"); // usado por Uploadfoto
    }
}
