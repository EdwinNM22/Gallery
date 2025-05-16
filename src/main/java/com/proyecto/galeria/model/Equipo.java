package com.proyecto.galeria.model;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "equipos")
public class Equipo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String nombre;
    private String descripcion;



    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "album_id")
    private album proyecto;

    @ManyToMany(fetch = FetchType.LAZY) // Añadido FetchType.LAZY
    @JoinTable(
            name = "equipo_usuario",
            joinColumns = @JoinColumn(name = "equipo_id"),
            inverseJoinColumns = @JoinColumn(name = "usuario_id")
    )
    private List<usuario> miembros = new ArrayList<>();

    // Constructores, getters y setters


public Equipo() {}

    public Equipo(Integer id, String nombre, String descripcion,  album proyecto, List<usuario> miembros) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;

        this.proyecto = proyecto;
        this.miembros = miembros;
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

    public album getProyecto() {
        return proyecto;
    }

    public void setProyecto(album proyecto) {
        this.proyecto = proyecto;
    }

    public List<usuario> getMiembros() {
        return miembros;
    }

    public void setMiembros(List<usuario> miembros) {
        this.miembros = miembros;
    }
}