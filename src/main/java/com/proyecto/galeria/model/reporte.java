package com.proyecto.galeria.model;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "reportes")
public class reporte {

    public enum Estado { DRAFT, SENT }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /* ──────────── RELATIONS ──────────── */

    @ManyToOne(optional = false)
    private album album;

    @ManyToOne(optional = false)
    private usuario autor;

    /* ──────────── PAYLOAD ──────────── */

    @Lob
    private String contenido;

    private LocalDateTime fechaHora;

    @Enumerated(EnumType.STRING)
    private Estado estado = Estado.DRAFT;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public album getAlbum() {
        return album;
    }

    public void setAlbum(album album) {
        this.album = album;
    }

    public usuario getAutor() {
        return autor;
    }

    public void setAutor(usuario autor) {
        this.autor = autor;
    }

    public String getContenido() {
        return contenido;
    }

    public void setContenido(String contenido) {
        this.contenido = contenido;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }

    public Estado getEstado() {
        return estado;
    }

    public void setEstado(Estado estado) {
        this.estado = estado;
    }

}
