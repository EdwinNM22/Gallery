package com.proyecto.galeria.model;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "fotos")
public class foto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String nombre;
    private String descripcion;
    private String imagen;

    @ManyToOne
    private usuario usuario;

    @ManyToMany(mappedBy = "fotos")
    private List<album> albumes;

    // Relación ManyToOne con SubAlbum
    @ManyToOne
    @JoinColumn(name = "sub_album_id") // Columna que referencia al subálbum
    private SubAlbum subAlbum;

    // Constructor, getters, setters y toString
    public foto() {}

    public foto(Integer id, String nombre, String descripcion, String imagen, usuario usuario) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.imagen = imagen;
        this.usuario = usuario;
    }

    // Getter y Setter para subAlbum
    public SubAlbum getSubAlbum() {
        return subAlbum;
    }

    public void setSubAlbum(SubAlbum subAlbum) {
        this.subAlbum = subAlbum;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getImagen() {
        return imagen;
    }

    public void setImagen(String imagen) {
        this.imagen = imagen;
    }

    public usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(usuario usuario) {
        this.usuario = usuario;
    }

    public List<album> getAlbumes() {
        return albumes;
    }

    public void setAlbumes(List<album> albumes) {
        this.albumes = albumes;
    }
}