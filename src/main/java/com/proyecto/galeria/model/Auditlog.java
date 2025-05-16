package com.proyecto.galeria.model;


import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_log")
public class Auditlog {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String action; // CREATE, UPDATE, DELETE, etc.

    private String entityName; // Ejemplo: "Album", "Photo"

    private Integer entityId; // ID del objeto afectado

    private String username; // Usuario que hizo el cambio

    private LocalDateTime timestamp; // Cuándo ocurrió

    @Column(columnDefinition = "TEXT")
    private String details; // Info adicional


    public Auditlog() {}

    public Auditlog(Integer id, String action, String entityName, Integer entityId, String username, LocalDateTime timestamp, String details) {
        this.id = id;
        this.action = action;
        this.entityName = entityName;
        this.entityId = entityId;
        this.username = username;
        this.timestamp = timestamp;
        this.details = details;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getEntityName() {
        return entityName;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    public Integer getEntityId() {
        return entityId;
    }

    public void setEntityId(Integer entityId) {
        this.entityId = entityId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }
}
