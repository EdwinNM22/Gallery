package com.proyecto.galeria.model;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "subalbumes")
public class SubAlbum {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String nombre;
    private String descripcion;
    @Column(name = "tipo")
    private String tipo;

    @ManyToOne
    @JoinColumn(name = "album_id")
    private album album;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private usuario usuario;

    @ManyToMany(cascade = CascadeType.REMOVE)
    @JoinTable(
            name = "subalbum_foto",
            joinColumns = @JoinColumn(name = "subalbum_id"),
            inverseJoinColumns = @JoinColumn(name = "foto_id")
    )
    private List<foto> fotos;


    public SubAlbum() {}

    // Constructor con nombre y el álbum asociado


    public SubAlbum(Integer id, String nombre, String descripcion, String tipo, album album, usuario usuario) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.tipo = tipo;
        this.album = album;
        this.usuario = usuario;
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

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public album getAlbum() {
        return album;
    }

    public void setAlbum(album album) {
        this.album = album;
    }

    public usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(usuario usuario) {
        this.usuario = usuario;
    }

    public List<foto> getFotos() {
        return fotos;
    }

    public void setFotos(List<foto> fotos) {
        this.fotos = fotos;
    }

    @Override
    public String toString() {
        return "SubAlbum{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", descripcion='" + descripcion + '\'' +
                ", tipo='" + tipo + '\'' +
                ", album=" + album +
                ", usuario=" + usuario +
                ", fotos=" + fotos +
                '}';
    }
}
