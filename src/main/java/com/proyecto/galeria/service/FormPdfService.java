package com.proyecto.galeria.service;

import com.proyecto.galeria.model.Form;
// import com.proyecto.galeria.model.FotosForm; // Not directly used in the method signature, but good to keep if needed elsewhere
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.xhtmlrenderer.pdf.ITextRenderer; // Assuming you're using Flying Saucer (xhtmlrenderer)

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Locale; // Don't forget this import!
import java.util.Map; // Don't forget this import for List<Map<String, String>>

@Service
public class FormPdfService {

    private final TemplateEngine templateEngine;

    public FormPdfService(TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    /**
     * Builds a PDF from a Thymeleaf template, incorporating form data and photos,
     * and applying the specified locale for internationalization.
     *
     * @param form   The Form object containing the main data.
     * @param fotos  A list of maps, where each map represents a photo with its
     *               base64 image and description.
     * @param titulo The main title for the PDF, already localized by the
     *               controller.
     * @param locale The desired locale for translating text within the PDF
     *               template.
     * @return A byte array representing the generated PDF.
     * @throws Exception If an error occurs during PDF generation.
     */
    public byte[] buildPdf(Form form, List<Map<String, String>> fotos, String titulo, Locale locale) throws Exception {
        // Crucial: Pass the locale to the Thymeleaf Context constructor
        // This ensures that th:text="#{key}" in your template resolves messages for the
        // correct language.
        Context context = new Context(locale);

        context.setVariable("form", form);
        context.setVariable("fotos", fotos);
        context.setVariable("titulo", titulo); // This 'titulo' is already localized from the controller

        // Process the Thymeleaf template to get the HTML content
        // Ensure "form/formPdf" correctly points to your HTML template file
        // (e.g., src/main/resources/templates/form/formPdf.html)
        String htmlContent = templateEngine.process("form/formPdf", context);

        // Use ITextRenderer (Flying Saucer) to convert HTML to PDF
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ITextRenderer renderer = new ITextRenderer();
        renderer.setDocumentFromString(htmlContent);

        // Important for fonts and special characters (like accents in Spanish)
        // You might need to add specific font resolvers if you have custom fonts or
        // are experiencing issues with certain characters not rendering correctly.
        // Example: renderer.getFontResolver().addFont("path/to/font.ttf",
        // BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);

        renderer.layout();
        renderer.createPDF(baos);
        baos.close(); // Close the output stream

        return baos.toByteArray();
    }
}