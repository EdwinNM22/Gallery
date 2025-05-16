package com.proyecto.galeria.service;

import com.proyecto.galeria.model.reporte;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.ByteArrayOutputStream;
import java.util.List;

@Service
public class ReportePdfService {

    private final TemplateEngine templateEngine;

    public ReportePdfService(TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    public byte[] buildPdf(List<reporte> reportes, String titulo) throws Exception {
        // Procesar la plantilla Thymeleaf
        Context context = new Context();
        context.setVariable("reportes", reportes);
        context.setVariable("titulo", titulo);

        // Cambiado para usar la ruta correcta
        String htmlContent = templateEngine.process("reportes/reportStyle", context);

        // Convertir HTML a PDF
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ITextRenderer renderer = new ITextRenderer();

        renderer.setDocumentFromString(htmlContent);
        renderer.layout();
        renderer.createPDF(baos);

        baos.close();
        return baos.toByteArray();
    }
}