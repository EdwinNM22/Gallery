package com.proyecto.galeria.controller;

import com.proyecto.galeria.model.Expediente;
import com.proyecto.galeria.model.Form;
import com.proyecto.galeria.model.FotosForm;
import com.proyecto.galeria.model.usuario;
import com.proyecto.galeria.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import javax.servlet.http.HttpSession;


@Controller
@RequestMapping("/form")
public class FormController {

    @Autowired
    private FormService formService;

    @Autowired
    private FotosFormService fotosFormService;

    @Autowired
    private UploadFormFoto uploadFormFoto;

    @Autowired
    private FormPdfService formPdfService;
    @Autowired
    private IUsuarioService usuarioService;

    @Autowired
    private PermisoService permisoService;

    @GetMapping
    public String showFormPage(@RequestParam(required = false) Integer expedienteId,
                               HttpSession session, Model model) {
        Integer idUsuario = (Integer) session.getAttribute("idusuario");
        if (idUsuario != null) {
            usuario user = usuarioService.findById(idUsuario).orElse(null);
            if (user != null) {
                model.addAttribute("nombreEvaluador", user.getNombre());
            }
        }
        model.addAttribute("expedienteId", expedienteId);
        return "form/Form.html";
    }


    @GetMapping("/manage/{usuarioId}/{expedienteId}")
    public String showManageFormPageByUsuario(
            @PathVariable Integer usuarioId,
            @PathVariable Integer expedienteId,
            Model model,
            HttpSession session) {
        try {
            Integer idUsuario = (Integer) session.getAttribute("idusuario");
            Optional<usuario> optionalUsuario = usuarioService.findById(idUsuario);
            if (optionalUsuario.isEmpty()) return "redirect:/NoAccess/Access";
            usuario usuario = optionalUsuario.get();

            if (usuario.getPermisos().stream().noneMatch(p -> "EXPEDIENTE_ACCESS".equals(p.getCodigo()))) {
                return "redirect:/NoAccess/Access";
            }

            List<Form> forms;

            // 🔥 Mostrar todos los formularios de ese expediente si es EDGAR o tiene permiso de ver todo
            boolean puedeVerTodos = "EDGAR".equalsIgnoreCase(usuario.getTipo_usuario()) ||
                    usuario.getPermisos().stream().anyMatch(p -> "EXPEDIENTE_VIEW_ALL".equals(p.getCodigo()));

            if (puedeVerTodos) {
                forms = formService.findByExpedienteId(expedienteId);
            } else {
                forms = formService.findByUsuarioIdAndExpedienteId(idUsuario, expedienteId);
            }

            model.addAttribute("forms", forms);
            model.addAttribute("expedienteId", expedienteId);
            model.addAttribute("permisos", permisoService.getAllPermisos());
            model.addAttribute("permisosAgrupados", permisoService.getPermisosAgrupadosPorVista());

            usuario.getPermisos().size();
            Set<String> permisos = usuario.getPermisos().stream().map(p -> p.getCodigo()).collect(Collectors.toSet());

            model.addAttribute("EXPEDIENTE_ACCESS", permisos.contains("EXPEDIENTE_ACCESS"));
            model.addAttribute("EXPEDIENTE_CREATE", permisos.contains("EXPEDIENTE_CREATE"));
            model.addAttribute("EXPEDIENTE_EDIT", permisos.contains("EXPEDIENTE_EDIT"));
            model.addAttribute("EXPEDIENTE_DELETE", permisos.contains("EXPEDIENTE_DELETE"));
            model.addAttribute("EXPEDIENTE_FORM_CREATE", permisos.contains("EXPEDIENTE_FORM_CREATE"));
            model.addAttribute("EXPEDIENTE_FORM_PDF", permisos.contains("EXPEDIENTE_FORM_PDF"));
            model.addAttribute("EXPEDIENTE_FORM_EDIT", permisos.contains("EXPEDIENTE_FORM_EDIT"));
            model.addAttribute("EXPEDIENTE_FORM_DELETE", permisos.contains("EXPEDIENTE_FORM_DELETE"));

        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error loading forms: " + e.getMessage());
        }
        return "form/ManageForms.html";
    }




    @PostMapping("/submit")
    public String submitForm(@ModelAttribute Form form,
                             @RequestParam(value = "fotos", required = false) MultipartFile[] fotos,
                             @RequestParam Map<String, String> descripcionesMap,
                             @RequestParam(value = "expedienteId", required = false) Integer expedienteId,
                             HttpSession session,
                             Model model) {
        try {
            Integer idUsuario = (Integer) session.getAttribute("idusuario");
            usuario usuario = usuarioService.findById(idUsuario)
                    .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));
            form.setUsuario(usuario);

            if (expedienteId != null) {
                Expediente expediente = new Expediente();
                expediente.setId(expedienteId);
                form.setExpediente(expediente);
            }

            if (fotos == null) fotos = new MultipartFile[0];
            Form savedForm = formService.save(form);

            if (fotos.length > 0) {
                for (int i = 0; i < fotos.length; i++) {
                    MultipartFile foto = fotos[i];
                    if (foto != null && !foto.isEmpty()) {
                        String filename = uploadFormFoto.saveImage(foto);
                        FotosForm fotosForm = new FotosForm();
                        fotosForm.setForm(savedForm);
                        fotosForm.setImagen(filename);
                        String descKey = "descripciones[" + i + "]";
                        String descripcion = descripcionesMap.getOrDefault(descKey, "");
                        fotosForm.setDescripcion(descripcion);
                        fotosFormService.save(fotosForm);
                    }
                }
            }

            model.addAttribute("success", true);
            model.addAttribute("message", "Formulario enviado exitosamente con " +
                    (fotos != null ? fotos.length : 0) + " foto(s)");

        } catch (Exception e) {
            System.err.println("Error submitting form: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", true);
            model.addAttribute("message", "Error al enviar el formulario: " + e.getMessage());
        }

        return "form/Form.html";
    }





    @GetMapping("/{id}")
    public String getFormById(@PathVariable Integer id, Model model) {
        try {
            Form form = formService.findById(id).orElse(null);
            if (form == null) {
                model.addAttribute("errorMessage", "Form not found");
                return "redirect:/form/manage";
            }
            // Get photos for this form
            List<FotosForm> fotos = fotosFormService.findByFormId(id);

            List<String> base64Fotos = new java.util.ArrayList<>();
            for (FotosForm foto : fotos) {
                String filename = foto.getImagen();
                String imagePath = "C:\\Users\\Alex\\Documents\\images\\form" + filename;
                try {
                    byte[] imageBytes = java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(imagePath));
                    String base64Image = java.util.Base64.getEncoder().encodeToString(imageBytes);
                    base64Fotos.add(base64Image);
                } catch (Exception ex) {
                }
            }
            model.addAttribute("fotos", base64Fotos);
            model.addAttribute("form", form);
            return "form/ViewForm.html";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error loading form: " + e.getMessage());
            return "redirect:/form/manage";
        }
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> downloadFormPdf(@PathVariable Integer id) {
        try {
            Form form = formService.findById(id).orElse(null);
            if (form == null) {
                return ResponseEntity.notFound().build();
            }
            List<FotosForm> fotos = fotosFormService.findByFormId(id);
            // Prepare Base64 images for PDF
            List<java.util.Map<String, String>> fotosBase64 = new java.util.ArrayList<>();
            for (FotosForm foto : fotos) {
                String filename = foto.getImagen();
                String imagePath = "C:\\Users\\Alex\\Documents\\images\\form" + filename;
                try {
                    byte[] imageBytes = java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(imagePath));
                    String base64Image = java.util.Base64.getEncoder().encodeToString(imageBytes);
                    java.util.Map<String, String> fotoMap = new java.util.HashMap<>();
                    fotoMap.put("imagenBase64", base64Image);
                    fotosBase64.add(fotoMap);
                } catch (Exception ex) {
                    // skip image if error
                }
            }
            String title = "Formulaire – " + form.getNombreCliente();
            byte[] pdf = formPdfService.buildPdf(form, fotosBase64, title);
            String fname = safeFileName(form.getNombreCliente()) + "_form_" + id + ".pdf";
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fname + "\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdf);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    private static String safeFileName(String raw) {
        return raw == null ? "form" : raw.replaceAll("[\\\\/:*?\"<>|]", "_").replaceAll("\\s+", "_");
    }

    @PostMapping
    public ResponseEntity<Form> createForm(@RequestBody Form form) {
        try {
            Form savedForm = formService.save(form);
            return ResponseEntity.ok(savedForm);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Form> updateForm(@PathVariable Integer id, @RequestBody Form form) {
        try {
            Form updated = formService.update(id, form);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteForm(@PathVariable Integer id) {
        try {
            formService.delete(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{id}/photos")
    public ResponseEntity<List<FotosForm>> getFormPhotos(@PathVariable Integer id) {
        try {
            List<FotosForm> photos = fotosFormService.findByFormId(id);
            return ResponseEntity.ok(photos);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{id}/edit")
    public String editFormPage(@PathVariable Integer id, Model model) {
        try {
            Form form = formService.findById(id).orElse(null);
            if (form == null) {
                model.addAttribute("errorMessage", "Form not found");
                return "redirect:/form/manage";
            }
            model.addAttribute("form", form);
            return "form/EditForm.html";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error loading form: " + e.getMessage());
            return "redirect:/form/manage";
        }
    }

    @PostMapping("/{id}/edit")
    public String updateForm(@PathVariable Integer id, @ModelAttribute Form newForm, Model model) {
        try {
            formService.update(id, newForm);
            return "redirect:/form/manage";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error updating form: " + e.getMessage());
            return "form/EditForm.html";
        }
    }
}