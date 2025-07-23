package com.proyecto.galeria.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class UploadFormFoto {

    private String folder = "/opt/Gallery/form/";
    private final Path root = Paths.get("images/form");

    public UploadFormFoto() {
        // Create the images directory if it doesn't exist
        Path imagesDir = Paths.get("images");
        if (!Files.exists(imagesDir)) {
            try {
                Files.createDirectory(imagesDir);
            } catch (IOException e) {
                System.out.println("No se puede crear el directorio images");
                throw new RuntimeException(e);
            }
        }

        // Create the form subdirectory if it doesn't exist
        if (!Files.exists(root)) {
            try {
                Files.createDirectory(root);
            } catch (IOException e) {
                System.out.println("No se puede crear el directorio form");
                throw new RuntimeException(e);
            }
        }

        // Create the absolute path directory if it doesn't exist
        Path absoluteDir = Paths.get(folder);
        if (!Files.exists(absoluteDir)) {
            try {
                Files.createDirectories(absoluteDir);
                System.out.println("Created directory: " + absoluteDir.toAbsolutePath());
            } catch (IOException e) {
                System.out.println("No se puede crear el directorio absoluto: " + absoluteDir);
                throw new RuntimeException(e);
            }
        }
    }

    public String saveImage(MultipartFile file) throws IOException {
        if (!file.isEmpty()) {
            byte[] bytes = file.getBytes();

            // Generate unique filename with UUID while preserving extension
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String uniqueFilename = UUID.randomUUID().toString() + extension;

            Path path = Paths.get(folder + uniqueFilename);
            Files.write(path, bytes);
            System.out.println("File saved successfully to: " + path.toAbsolutePath());
            return uniqueFilename;
        } else {
            File filesource = new File("/opt/default.jpg");
            File fileDest = new File("default.jpg");
            InputStream in = new FileInputStream(filesource);
            Path path = Paths.get(folder + fileDest);
            Files.write(path, in.readAllBytes());
        }

        return "default.jpg";
    }

    public void deleteImage(String filename) {
        String ruta = "/opt/Gallery/form/";
        File file = new File(ruta + filename);
        if (file.delete()) {
            System.out.println("Form image deleted successfully");
        } else {
            System.out.println("Error: Form image not deleted");
        }
    }
}