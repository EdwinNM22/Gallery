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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
@RequestMapping("/form")
public class FormController {
    private static final Logger logger = LoggerFactory.getLogger(FormController.class);

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

    @Autowired
    private ExpedienteService expedienteService;

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
            // if (optionalUsuario.isEmpty()) return "redirect:/NoAccess/Access";
            usuario usuario = optionalUsuario.get();

            // if (usuario.getPermisos().stream().noneMatch(p ->
            // "EXPEDIENTE_ACCESS".equals(p.getCodigo()))) {
            // return "redirect:/NoAccess/Access";
            // }

            List<Form> forms;

            // 🔥 Mostrar todos los formularios de ese expediente si es EDGAR o tiene
            // permiso de ver todo
            boolean puedeVerTodos = "EDGAR".equalsIgnoreCase(usuario.getTipo_usuario()) ||
                    usuario.getPermisos().stream().anyMatch(p -> "EXPEDIENTE_VIEW_ALL".equals(p.getCodigo()));

            if (puedeVerTodos) {
                forms = formService.findByExpedienteIdAndFuturo(expedienteId, false);
            } else {
                forms = formService.findByUsuarioIdAndExpedienteIdAndFuturo(idUsuario, expedienteId, false);
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

    // @PostMapping("/submitT")
    // public String submitForm(@ModelAttribute Form form,
    // @RequestParam("fotos") MultipartFile[] files, // This captures the uploaded
    // images
    // @RequestParam(value = "fotos", required = false) MultipartFile[] fotos,
    // @RequestParam Map<String, String> descripcionesMap,
    // @RequestParam(value = "expedienteId", required = false) Integer expedienteId,
    // HttpSession session,
    // HttpServletRequest request,
    // Model model) {
    // try {
    // Integer idUsuario = (Integer) session.getAttribute("idusuario");
    // usuario usuario = usuarioService.findById(idUsuario)
    // .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));
    // form.setUsuario(usuario);

    // if (expedienteId != null) {
    // Expediente expediente = new Expediente();
    // expediente.setId(expedienteId);
    // form.setExpediente(expediente);
    // }

    // if (fotos == null)
    // fotos = new MultipartFile[0];
    // Form savedForm = formService.save(form);

    // if (fotos.length > 0) {
    // for (int i = 0; i < fotos.length; i++) {
    // MultipartFile foto = fotos[i];
    // if (foto != null && !foto.isEmpty()) {
    // String filename = uploadFormFoto.saveImage(foto);
    // FotosForm fotosForm = new FotosForm();
    // fotosForm.setForm(savedForm);
    // fotosForm.setImagen(filename);
    // String descKey = "descripciones[" + i + "]";
    // String descripcion = descripcionesMap.getOrDefault(descKey, "");
    // fotosForm.setDescripcion(descripcion);
    // fotosFormService.save(fotosForm);
    // }
    // }
    // }

    // model.addAttribute("success", true);
    // model.addAttribute("message", "Formulario enviado exitosamente con " +
    // (fotos != null ? fotos.length : 0) + " foto(s)");

    // String referer = request.getHeader("Referer");
    // return "redirect:" + referer;
    // } catch (Exception e) {
    // System.err.println("Error submitting form: " + e.getMessage());
    // e.printStackTrace();
    // model.addAttribute("error", true);
    // model.addAttribute("message", "Error al enviar el formulario: " +
    // e.getMessage());
    // }

    // return "form/Form.html";
    // }

    @PostMapping("/submit")
    public String submitForm(@ModelAttribute Form form,
            @RequestParam(value = "expedienteId", required = false) Integer expedienteId,
            @RequestParam("fotos") MultipartFile[] fotos,
            @RequestParam(value = "photoDescriptions", required = false) String[] descriptions,
            HttpSession session) throws IOException {
        try {
            // --- Encontrar Usuario
            Integer idUsuario = (Integer) session.getAttribute("idusuario");
            usuario usuario = usuarioService.findById(idUsuario)
                    .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

            // --- Encontrar Expediente ---
            Expediente expediente = expedienteService.findById(expedienteId);

            if (expediente == null) {
                expediente = new Expediente();
                expediente.setId(expedienteId);
            }

            // --- Guardar Form ---
            form.setUsuario(usuario);
            form.setExpediente(expediente);
            Form savedForm = formService.save(form);

            // --- Procesar Fotos ---

            if (fotos != null && fotos.length > 0) {
                for (int i = 0; i < fotos.length; i++) {
                    MultipartFile foto = fotos[i];
                    if (!foto.isEmpty()) {
                        String filename = uploadFormFoto.saveImage(foto);
                        FotosForm fotosForm = new FotosForm();
                        fotosForm.setForm(savedForm);
                        fotosForm.setImagen(filename);

                        // Get description (safe handling for array bounds)
                        String description = (descriptions != null && i < descriptions.length)
                                ? descriptions[i]
                                : "";
                        fotosForm.setDescripcion(description);

                        fotosFormService.save(fotosForm);
                    }
                }
            }

        } catch (Exception e) {

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

            // Obtener fotos del formulario
            List<FotosForm> fotos = fotosFormService.findByFormId(id);

            List<Map<String, String>> fotosConDescripcion = new ArrayList<>();
            for (FotosForm foto : fotos) {
                String filename = foto.getImagen();
                String imagePath = "/opt/Gallery/form/" + filename;
                try {
                    byte[] imageBytes = java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(imagePath));
                    String base64Image = java.util.Base64.getEncoder().encodeToString(imageBytes);
                    Map<String, String> fotoData = new HashMap<>();
                    fotoData.put("imagenBase64", base64Image);
                    fotoData.put("descripcion", foto.getDescripcion()); // Asegúrate que exista el campo
                    fotosConDescripcion.add(fotoData);
                } catch (Exception ex) {
                    // Manejo de error (puedes registrar el error si quieres)
                }
            }

            model.addAttribute("fotos", fotosConDescripcion);
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
                String imagePath = "/opt/Gallery/form/" + filename;
                try {
                    byte[] imageBytes = java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(imagePath));
                    String base64Image = java.util.Base64.getEncoder().encodeToString(imageBytes);
                    java.util.Map<String, String> fotoMap = new java.util.HashMap<>();
                    fotoMap.put("imagenBase64", base64Image);
                    fotoMap.put("descripcion", foto.getDescripcion()); // <-- agrega la descripcion
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

    @GetMapping("/{id}/photos")
    public ResponseEntity<List<FotosForm>> getFormPhotos(@PathVariable Integer id) {
        try {
            List<FotosForm> photos = fotosFormService.findByFormId(id);
            return ResponseEntity.ok(photos);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // Controller completo con edición de formulario + fotos descripción

    @GetMapping("/{id}/edit")
    public String editFormPage(@PathVariable Integer id, Model model,
            @RequestParam(value = "success", required = false) String success) {
        try {
            Form form = formService.findById(id).orElse(null);
            if (form == null) {
                model.addAttribute("errorMessage", "Form not found");
                return "redirect:/form/manage";
            }

            List<FotosForm> fotos = fotosFormService.findByFormId(id);
            List<Map<String, String>> fotosConDescripcion = new ArrayList<>();
            for (FotosForm foto : fotos) {
                String filename = foto.getImagen();
                String imagePath = "/opt/Gallery/form/" + filename;
                try {
                    byte[] imageBytes = java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(imagePath));
                    String base64Image = java.util.Base64.getEncoder().encodeToString(imageBytes);
                    Map<String, String> fotoData = new HashMap<>();
                    fotoData.put("id", String.valueOf(foto.getId()));
                    fotoData.put("imagenBase64", base64Image);
                    fotoData.put("descripcion", foto.getDescripcion());
                    fotosConDescripcion.add(fotoData);
                } catch (Exception ex) {
                    // ignorar error de imagen faltante
                }
            }

            model.addAttribute("form", form);
            model.addAttribute("fotos", fotosConDescripcion);

            if (success != null) {
                model.addAttribute("success", true);
            }
            return "form/EditForm.html";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error loading form: " + e.getMessage());
            return "form/EditForm.html";
        }
    }

    @Transactional
    @PostMapping("/{id}/edit")
    public String updateForm(@PathVariable Integer id,
            @ModelAttribute("form") Form form, // Make sure this matches your model attribute name
            @RequestParam(value = "fotos", required = false) MultipartFile[] newPhotos,
            @RequestParam(value = "photoDescriptions", required = false) String[] newPhotoDescriptions,
            @RequestParam(value = "existingPhotoIds", required = false) Integer[] existingPhotoIds,
            @RequestParam(value = "existingPhotoDescriptions", required = false) String[] existingPhotoDescriptions,
            @RequestParam(value = "fotosToDelete", required = false) String photosToDelete,
            Model model,
            RedirectAttributes redirectAttributes) throws IOException {
        try {
            System.out.println("New photos: " + newPhotos);
            System.out.println("New Photo Descrips: " + newPhotoDescriptions);
            System.out.println("Existing Photo Ids: " + existingPhotoIds);
            System.out.println("Existing Photo Desc :" + existingPhotoDescriptions);
            System.out.println("Photos to delete:" + photosToDelete);

            // 1. Apply Form Changes
            Form updatedForm = formService.update(id, form);

            // 2. Delete Selected Photos
            if (photosToDelete != null && !photosToDelete.isEmpty()) {
                List<Integer> idsToDelete = Arrays.stream(photosToDelete.split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .map(Integer::parseInt)
                        .collect(Collectors.toList());
                fotosFormService.deleteByIds(idsToDelete);
            }

            // 3. Update existing photos' descriptions
            if (existingPhotoIds != null && existingPhotoDescriptions != null
                    && existingPhotoIds.length == existingPhotoDescriptions.length) {
                for (int i = 0; i < existingPhotoIds.length; i++) {
                    Optional<FotosForm> existingPhoto = fotosFormService.findById(existingPhotoIds[i]);
                    if (existingPhoto.isPresent()) {
                        FotosForm foto = existingPhoto.get();
                        foto.setDescripcion(existingPhotoDescriptions[i]);
                        fotosFormService.save(foto);
                    }
                }
            }

            // -- Add New Photos --
            if (newPhotos != null && newPhotos.length > 0) {
                for (int i = 0; i < newPhotos.length; i++) {
                    if (!newPhotos[i].isEmpty()) {
                        String filename = uploadFormFoto.saveImage(newPhotos[i]);
                        FotosForm newFoto = new FotosForm();
                        newFoto.setForm(form);
                        newFoto.setImagen(filename);
                        newFoto.setDescripcion(
                                (newPhotoDescriptions != null && i < newPhotoDescriptions.length)
                                        ? newPhotoDescriptions[i]
                                        : "");
                        fotosFormService.save(newFoto);
                    }
                }
            }

        } catch (Exception e) {
            model.addAttribute("error", "Error updating form: " + e.getMessage());
            return "form/EditForm.html";
        }

        return "redirect:/form/" + id + "/edit?success";
    }

    @PostMapping("/{id}/delete")
    @Transactional
    public String deleteForm(@PathVariable Integer id,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {
        logger.info("DELETE REQUEST - Attempting to delete form with ID: {}", id);

        try {
            // Check if form exists
            boolean exists = formService.existsById(id);
            logger.info("Form exists check: {}", exists);

            if (!exists) {
                logger.warn("Form with ID {} not found", id);
                redirectAttributes.addFlashAttribute("errorMessage", "Form not found");
                return getRedirectUrl(request);
            }

            // First, get associated photos
            List<FotosForm> fotos = fotosFormService.findByFormId(id);
            logger.info("Found {} photos associated with form ID {}", fotos.size(), id);

            // Delete physical files
            for (FotosForm foto : fotos) {
                String imagePath = "/opt/Gallery/form/" + foto.getImagen();
                logger.info("Attempting to delete image file: {}", imagePath);
                try {
                    boolean deleted = java.nio.file.Files.deleteIfExists(java.nio.file.Paths.get(imagePath));
                    logger.info("Image file deleted: {}", deleted);
                } catch (Exception e) {
                    logger.error("Error deleting image file {}: {}", imagePath, e.getMessage());
                }
            }

            // Delete photos from database
            logger.info("Deleting photos from database for form ID: {}", id);
            fotosFormService.deleteByFormId(id);

            // Delete the form
            logger.info("Deleting form with ID: {}", id);
            formService.delete(id);

            logger.info("Form {} deleted successfully", id);
            redirectAttributes.addFlashAttribute("successMessage", "Form deleted successfully");

        } catch (Exception e) {
            logger.error("Error deleting form with ID {}: {}", id, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting form: " + e.getMessage());
        }

        return getRedirectUrl(request);
    }

    private String getRedirectUrl(HttpServletRequest request) {
        String referer = request.getHeader("Referer");
        logger.info("Referer URL: {}", referer);
        if (referer != null && !referer.isEmpty()) {
            return "redirect:" + referer;
        } else {
            return "redirect:/form/manage";
        }
    }
}
