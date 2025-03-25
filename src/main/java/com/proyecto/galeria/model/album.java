package com.proyecto.galeria.model;



import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "albumes")
public class album {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String nombre;
    private String descripcion;

    @ManyToMany(cascade = CascadeType.ALL)  // Agregado CascadeType.ALL
    @JoinTable(
            name = "album_foto",
            joinColumns = @JoinColumn(name = "album_id"),
            inverseJoinColumns = @JoinColumn(name = "foto_id")
    )
    private List<foto> fotos;

    @ManyToOne(cascade = CascadeType.ALL)  // Agregado CascadeType.ALL
    private usuario usuario;

    // Getters y setters
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

    public List<foto> getFotos() {
        return fotos;
    }

    public void setFotos(List<foto> fotos) {
        this.fotos = fotos;
    }

    public usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(usuario usuario) {
        this.usuario = usuario;
    }

    @Override
    public String toString() {
        return "album{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", descripcion='" + descripcion + '\'' +
                ", fotos=" + fotos +
                ", usuario=" + usuario +
                '}';
    }
}
