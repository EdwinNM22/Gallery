package com.proyecto.galeria.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class Uploadfoto {
    private String folder = "images//";

    public String saveImage(MultipartFile file) throws IOException {
        if (!file.isEmpty()) {
            String originalFilename = file.getOriginalFilename();
            byte[] bytes = file.getBytes();
            Path path = Paths.get(folder + originalFilename);
            Files.write(path, bytes);

            return originalFilename;
        }
        return "default.jpg";
    }

    public void deleteImage(String nombre) {
        File file = new File(folder + nombre);
        file.delete();
    }
}

