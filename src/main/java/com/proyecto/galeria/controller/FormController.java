package com.proyecto.galeria.controller;

import com.proyecto.galeria.model.Expediente;
import com.proyecto.galeria.model.Form;
import com.proyecto.galeria.model.FotosForm;
import com.proyecto.galeria.model.usuario;
import com.proyecto.galeria.service.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.beans.PropertyEditorSupport;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
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
    private MessageSource messageSource;

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


    @InitBinder
    public void initBinder(WebDataBinder binder) {
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        binder.registerCustomEditor(LocalTime.class, new PropertyEditorSupport() {
            @Override
            public void setAsText(String text) {
                setValue(LocalTime.parse(text, timeFormatter));
            }
        });
    }

    @GetMapping("/manage/{usuarioId}")
    public String showManageFormPageByUsuario(
            @PathVariable Integer usuarioId,
            Model model,
            HttpSession session) {
        try {
            Integer idUsuario = (Integer) session.getAttribute("idusuario");
            Optional<usuario> optionalUsuario = usuarioService.findById(idUsuario);
            if (optionalUsuario.isEmpty())
                return "redirect:/NoAccess/Access";
            usuario usuario = optionalUsuario.get();

            List<Form> forms = formService.findByUsuarioId(idUsuario);
            model.addAttribute("forms", forms);
            model.addAttribute("permisos", permisoService.getAllPermisos());
            model.addAttribute("permisosAgrupados", permisoService.getPermisosAgrupadosPorVista());

            Set<String> permisos = usuario.getPermisos().stream().map(p -> p.getCodigo()).collect(Collectors.toSet());
            model.addAttribute("EXPEDIENTE_ACCESS", permisos.contains("EXPEDIENTE_ACCESS"));
            model.addAttribute("EXPEDIENTE_CREATE", permisos.contains("EXPEDIENTE_CREATE"));
            model.addAttribute("EXPEDIENTE_EDIT", permisos.contains("EXPEDIENTE_EDIT"));
            model.addAttribute("EXPEDIENTE_DELETE", permisos.contains("EXPEDIENTE_DELETE"));
            model.addAttribute("EXPEDIENTE_FORM_CREATE", permisos.contains("EXPEDIENTE_FORM_CREATE"));
            model.addAttribute("EXPEDIENTE_FORM_PDF", permisos.contains("EXPEDIENTE_FORM_PDF"));
            model.addAttribute("EXPEDIENTE_FORM_EDIT", permisos.contains("EXPEDIENTE_FORM_EDIT"));
            model.addAttribute("EXPEDIENTE_FORM_DELETE", permisos.contains("EXPEDIENTE_FORM_DELETE"));

            List<Form> futureForms = formService.findByUsuarioIdAndFuturo(idUsuario, true);
            List<Form> completeForms = formService.findByUsuarioIdAndFuturo(idUsuario, false);

            List<Map<String, String>> events = futureForms.stream()
                    .filter(f -> f.getFechaEvaluacion() != null && f.getHoraEvaluacion() != null)
                    .map(f -> {
                        LocalDate fecha = f.getFechaEvaluacion();
                        LocalTime hora = f.getHoraEvaluacion();
                        String startDateTime = fecha.atTime(hora).toString(); // ISO_LOCAL_DATE_TIME
                        return Map.of(
                                "id", f.getId().toString(),
                                "start", startDateTime,
                                "end", startDateTime,
                                "nombreCliente", f.getNombreProyecto()
                        );
                    })
                    .collect(Collectors.toList());

            List<Map<String, String>> eventsComplete = completeForms.stream()
                    .filter(f -> f.getFechaEvaluacion() != null && f.getHoraEvaluacion() != null)
                    .map(f -> {
                        LocalDate fecha = f.getFechaEvaluacion();
                        LocalTime hora = f.getHoraEvaluacion();
                        String startDateTime = fecha.atTime(hora).toString(); // ISO_LOCAL_DATE_TIME
                        return Map.of(
                                "id", f.getId().toString(),
                                "start", startDateTime,
                                "end", startDateTime,
                                "nombreCliente", f.getNombreProyecto()
                        );
                    })
                    .collect(Collectors.toList());

            model.addAttribute("events", events);
            model.addAttribute("eventsComplete", eventsComplete);


        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error loading forms: " + e.getMessage());
        }
        return "form/ManageForms.html";
    }

    @GetMapping
    public String showFormPage(
            @RequestParam(value = "success", required = false) String success,
            HttpSession session, Model model) {
        Integer idUsuario = (Integer) session.getAttribute("idusuario");
        if (idUsuario != null) {
            usuario user = usuarioService.findById(idUsuario).orElse(null);
            if (user != null) {
                model.addAttribute("nombreEvaluador", user.getNombre());
            }
        }

        if (success != null) {
            model.addAttribute("success", true);
        }

        return "form/Form.html";
    }

    @PostMapping("/submit")
    @ResponseBody
    public ResponseEntity<?> submitForm(
            @ModelAttribute Form form,
            @RequestParam(value = "fotos", required = false) MultipartFile[] fotos,
            @RequestParam(value = "photoDescriptions", required = false) String[] descriptions,
            @RequestParam("futuro") boolean futuro,
            HttpSession session) throws IOException {

        Map<String, Object> response = new HashMap<>();
        try {
            // --- Encontrar Usuario
            Integer idUsuario = (Integer) session.getAttribute("idusuario");
            usuario usuario = usuarioService.findById(idUsuario)
                    .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

            // --- Guardar Form ---
            form.setUsuario(usuario);
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

                        String description = (descriptions != null && i < descriptions.length)
                                ? descriptions[i]
                                : "";
                        fotosForm.setDescripcion(description);

                        fotosFormService.save(fotosForm);
                    }
                }
            }

            response.put("success", true);
            response.put("message", "Form submitted successfully");
            response.put("redirectUrl", futuro
                    ? "/expediente/create-future-project"
                    : "/form/");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error submitting form: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
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
                    fotoData.put("descripcion", foto.getDescripcion());
                    fotosConDescripcion.add(fotoData);
                } catch (Exception ex) {
                    // Manejo de error (opcional)
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
    public ResponseEntity<byte[]> downloadFormPdf(@PathVariable Integer id, Locale currentLocale) {
        try {
            Form form = formService.findById(id).orElse(null);
            if (form == null) {
                return ResponseEntity.notFound().build();
            }
            List<FotosForm> fotos = fotosFormService.findByFormId(id);
            List<Map<String, String>> fotosBase64 = new ArrayList<>();
            for (FotosForm foto : fotos) {
                String filename = foto.getImagen();
                String imagePath = "/opt/Gallery/form/" + filename;
                try {
                    byte[] imageBytes = java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(imagePath));
                    String base64Image = Base64.getEncoder().encodeToString(imageBytes);
                    Map<String, String> fotoMap = new HashMap<>();
                    fotoMap.put("imagenBase64", base64Image);
                    fotoMap.put("descripcion", foto.getDescripcion());
                    fotosBase64.add(fotoMap);
                } catch (Exception ex) {
                    System.err.println("Error loading image " + imagePath + ": " + ex.getMessage());
                }
            }

            String formTitleKey = "evaluation-form-title-full";
            String localizedFormTitle = messageSource.getMessage(formTitleKey, null, currentLocale);

            String titleForPdf = localizedFormTitle + " – " + form.getNombreCliente();

            byte[] pdf = formPdfService.buildPdf(form, fotosBase64, titleForPdf, currentLocale);

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
    @ResponseBody
    public ResponseEntity<?> updateForm(
            @PathVariable Integer id,
            @ModelAttribute("form") Form form,
            @RequestParam(value = "fotos", required = false) MultipartFile[] newPhotos,
            @RequestParam(value = "photoDescriptions", required = false) String[] newPhotoDescriptions,
            @RequestParam(value = "existingPhotoIds", required = false) Integer[] existingPhotoIds,
            @RequestParam(value = "existingPhotoDescriptions", required = false) String[] existingPhotoDescriptions,
            @RequestParam(value = "fotosToDelete", required = false) String photosToDelete,
            @RequestParam("futuro") boolean futuro,
            HttpServletRequest request) throws IOException {

        Map<String, Object> response = new HashMap<>();

        try {
            Optional<Form> optionalForm = formService.findById(id);
            Form originalForm = optionalForm.orElseThrow(() -> new RuntimeException("Form no encontrado"));


            BeanUtils.copyProperties(form, originalForm, getNullPropertyNames(form));

            formService.update(id, originalForm);

            if (photosToDelete != null && !photosToDelete.isEmpty()) {
                List<Integer> idsToDelete = Arrays.stream(photosToDelete.split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .map(Integer::parseInt)
                        .collect(Collectors.toList());

                List<FotosForm> fotosToDeleteFromDisk = fotosFormService.findAllById(idsToDelete);

                for (FotosForm foto : fotosToDeleteFromDisk) {
                    String imagePath = "/opt/Gallery/form/" + foto.getImagen();
                    try {
                        logger.info("Deleting image file during edit: {}", imagePath);
                        boolean deleted = java.nio.file.Files.deleteIfExists(java.nio.file.Paths.get(imagePath));
                        logger.info("Image file deleted during edit: {}", deleted);
                    } catch (Exception e) {
                        logger.error("Error deleting image file {} during edit: {}", imagePath, e.getMessage());
                    }
                }

                fotosFormService.deleteByIds(idsToDelete);
            }

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

            if (newPhotos != null && newPhotos.length > 0) {
                for (int i = 0; i < newPhotos.length; i++) {
                    if (!newPhotos[i].isEmpty()) {
                        String filename = uploadFormFoto.saveImage(newPhotos[i]);
                        FotosForm newFoto = new FotosForm();
                        newFoto.setForm(originalForm);
                        newFoto.setImagen(filename);
                        newFoto.setDescripcion(
                                (newPhotoDescriptions != null && i < newPhotoDescriptions.length)
                                        ? newPhotoDescriptions[i]
                                        : "");
                        fotosFormService.save(newFoto);
                    }
                }
            }

            response.put("success", true);
            response.put("message", "Form updated successfully");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error updating form: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    private String[] getNullPropertyNames(Object source) {
        final BeanWrapper src = new BeanWrapperImpl(source);
        java.beans.PropertyDescriptor[] pds = src.getPropertyDescriptors();

        Set<String> emptyNames = new HashSet<>();
        for (java.beans.PropertyDescriptor pd : pds) {
            Object srcValue = src.getPropertyValue(pd.getName());
            if (srcValue == null) emptyNames.add(pd.getName());
        }
        String[] result = new String[emptyNames.size()];
        return emptyNames.toArray(result);
    }


    @PostMapping("/{id}/delete")
    @Transactional
    public String deleteForm(@PathVariable Integer id,
                             HttpServletRequest request,
                             RedirectAttributes redirectAttributes) {
        logger.info("DELETE REQUEST - Attempting to delete form with ID: {}", id);

        try {
            boolean exists = formService.existsById(id);
            logger.info("Form exists check: {}", exists);

            if (!exists) {
                logger.warn("Form with ID {} not found", id);
                redirectAttributes.addFlashAttribute("errorMessage", "Form not found");
                return getRedirectUrl(request);
            }

            List<FotosForm> fotos = fotosFormService.findByFormId(id);
            logger.info("Found {} photos associated with form ID {}", fotos.size(), id);

            for (FotosForm foto : fotos) {
                String imagePath = "/opt/Gallery/form/" + foto.getImagen();
                logger.info("Deleting image file: {}", imagePath);
                try {
                    boolean deleted = java.nio.file.Files.deleteIfExists(java.nio.file.Paths.get(imagePath));
                    logger.info("Image file deleted: {}", deleted);
                } catch (Exception e) {
                    logger.error("Error deleting image file {}: {}", imagePath, e.getMessage());
                }
            }

            fotosFormService.deleteByFormId(id);

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