package com.proyecto.galeria.repository;

import com.proyecto.galeria.model.Auditlog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditLogRepository extends JpaRepository<Auditlog, Integer> {
}