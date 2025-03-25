package com.proyecto.galeria;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication
public class GaleriaApplication {

    public static void main(String[] args) {
        SpringApplication.run(GaleriaApplication.class, args);
    }
}
