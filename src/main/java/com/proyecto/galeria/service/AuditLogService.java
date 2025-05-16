package com.proyecto.galeria.service;

import com.proyecto.galeria.model.Auditlog;

import java.util.List;
import java.util.Optional;

public interface AuditLogService {
    public Auditlog save(Auditlog log);
    public Optional<Auditlog> get(Integer id);
    public List<Auditlog> findAll();
}
