package com.proyecto.galeria.service.Impl;

import com.proyecto.galeria.model.Auditlog;
import com.proyecto.galeria.repository.AuditLogRepository;
import com.proyecto.galeria.service.AuditLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class AuditLogServiceImpl implements AuditLogService {


    @Autowired
    private AuditLogRepository auditLogRepository;

    public AuditLogServiceImpl(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Override
    public Auditlog save(Auditlog log) {
        log.setTimestamp(LocalDateTime.now());
        return auditLogRepository.save(log);
    }

    @Override
    public Optional<Auditlog> get(Integer id) {
        return auditLogRepository.findById(id);
    }

    @Override
    public List<Auditlog> findAll() {
        return auditLogRepository.findAll();
    }
}