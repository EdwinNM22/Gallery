package com.proyecto.galeria.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import javax.persistence.*;
import java.util.*;


@Entity
@Table(name = "permisos")
public class Permiso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true)
    private String codigo; // Ejemplo: AGENDA_EDIT, USUARIOS_DELETE

    @Column(nullable = false)
    private String nombre; // Nombre legible: "Editar Agenda", "Eliminar Usuarios"

    private String descripcion;

    @Column(name = "grupo_vista", nullable = false)
    private String grupoVista; // "AGENDA", "USUARIOS", "PROYECTOS", etc.

    @Column(name = "tipo_permiso", nullable = false)
    private String tipoPermiso; // "ACCESS", "CREATE", "EDIT", "DELETE", etc.

    public Permiso() {}


    public Permiso(Integer id, String codigo, String nombre, String descripcion, String grupoVista, String tipoPermiso) {
        this.id = id;
        this.codigo = codigo;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.grupoVista = grupoVista;
        this.tipoPermiso = tipoPermiso;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
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

    public String getGrupoVista() {
        return grupoVista;
    }

    public void setGrupoVista(String grupoVista) {
        this.grupoVista = grupoVista;
    }

    public String getTipoPermiso() {
        return tipoPermiso;
    }

    public void setTipoPermiso(String tipoPermiso) {
        this.tipoPermiso = tipoPermiso;
    }
}
