package com.proyecto.galeria.service;

import com.proyecto.galeria.model.Form;
import com.proyecto.galeria.model.FotosForm;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.ByteArrayOutputStream;
import java.util.List;

@Service
public class FormPdfService {
    private final TemplateEngine templateEngine;

    public FormPdfService(TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    public byte[] buildPdf(Form form, List<?> fotos, String titulo) throws Exception {
        Context context = new Context();
        context.setVariable("form", form);
        context.setVariable("fotos", fotos);
        context.setVariable("titulo", titulo);
        String htmlContent = templateEngine.process("form/formPdf", context);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ITextRenderer renderer = new ITextRenderer();
        renderer.setDocumentFromString(htmlContent);
        renderer.layout();
        renderer.createPDF(baos);
        baos.close();
        return baos.toByteArray();
    }
}