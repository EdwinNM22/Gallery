package com.proyecto.galeria.controller;

import com.proyecto.galeria.model.FotosForm;
import com.proyecto.galeria.model.Form;
import com.proyecto.galeria.service.FotosFormService;
import com.proyecto.galeria.service.FormService;
import com.proyecto.galeria.service.UploadFormFoto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Controller
@RequestMapping("/fotos-form")
public class FotosFormController {

    @Autowired
    private FotosFormService fotosFormService;

    @Autowired
    private FormService formService;

    @Autowired
    private UploadFormFoto uploadFormFoto;

    // Add endpoint to get all FotosForm by formId
    @GetMapping("/../form/{formId}/fotos-form")
    public ResponseEntity<List<FotosForm>> getFotosFormsByFormId(@PathVariable Integer formId) {
        try {
            List<FotosForm> fotosForms = fotosFormService.findByFormId(formId);
            return ResponseEntity.ok(fotosForms);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // Add endpoint to create FotosForm only in context of a Form
    @PostMapping("/../form/{formId}/fotos-form")
    public ResponseEntity<FotosForm> createFotosFormForForm(@PathVariable Integer formId, @RequestParam("img") MultipartFile file) {
        try {
            Form form = formService.findById(formId)
                    .orElseThrow(() -> new RuntimeException("Form not found with id: " + formId));

            String filename = uploadFormFoto.saveImage(file);

            FotosForm fotosForm = new FotosForm();
            fotosForm.setForm(form);
            fotosForm.setImagen(filename);

            FotosForm savedFotosForm = fotosFormService.save(fotosForm);
            return ResponseEntity.ok(savedFotosForm);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFotosForm(@PathVariable Integer id) {
        try {
            FotosForm fotosForm = fotosFormService.findById(id)
                    .orElseThrow(() -> new RuntimeException("FotosForm not found with id: " + id));

            // Delete the image file from disk
            if (fotosForm.getImagen() != null && !fotosForm.getImagen().equals("default.jpg")) {
                uploadFormFoto.deleteImage(fotosForm.getImagen());
            }

            fotosFormService.delete(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}