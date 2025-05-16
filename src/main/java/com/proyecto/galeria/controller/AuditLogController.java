package com.proyecto.galeria.controller;


import com.proyecto.galeria.model.Auditlog;
import com.proyecto.galeria.repository.AuditLogRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.*;

@Controller
@RequestMapping("/audit")
public class AuditLogController {

    private final AuditLogRepository auditLogRepository;

    public AuditLogController(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @GetMapping
    public List<Auditlog> getAllLogs() {
        return auditLogRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Auditlog> getLogById(@PathVariable Integer id) {
        return auditLogRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}