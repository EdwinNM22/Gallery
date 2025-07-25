package com.proyecto.galeria.model;



import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
@Entity
@Table(name = "usuarios")
public class usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String nombre;
    private String email ;
    private String tipo_usuario;
    private String password;


    @OneToMany(mappedBy = "usuario", fetch = FetchType.LAZY)
    private List<foto> fotos;

    @OneToMany(mappedBy = "usuario", cascade = CascadeType.PERSIST) // O CascadeType.MERGE según el comportamiento deseado
    private List<album> albumes;


    //Para los permisos
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "usuario_permiso",
            joinColumns = @JoinColumn(name = "usuario_id"),
            inverseJoinColumns = @JoinColumn(name = "permiso_id")
    )

    private Set<Permiso> permisos = new HashSet<>();


    @OneToMany(mappedBy = "usuario")
    @JsonManagedReference
    private List<Expediente> expedientes;

    @OneToMany(mappedBy = "usuario")
    private List<Form> forms;



    public usuario() {}

    public usuario(Integer id, String nombre, String email, String tipo_usuario, String password) {
        this.id = id;
        this.nombre = nombre;
        this.email = email;
        this.tipo_usuario = tipo_usuario;
        this.password = password;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTipo_usuario() {
        return tipo_usuario;
    }

    public void setTipo_usuario(String tipo_usuario) {
        this.tipo_usuario = tipo_usuario;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public List<foto> getFotos() {
        return fotos;
    }

    public void setFotos(List<foto> fotos) {
        this.fotos = fotos;
    }

    public List<album> getAlbumes() {
        return albumes;
    }

    public void setAlbumes(List<album> albumes) {
        this.albumes = albumes;
    }

    public Set<Permiso> getPermisos() {
        return permisos;
    }

    public void setPermisos(Set<Permiso> permisos) {
        this.permisos = permisos;
    }

    public List<Expediente> getExpedientes() {
        return expedientes;
    }

    public void setExpedientes(List<Expediente> expedientes) {
        this.expedientes = expedientes;
    }

    public List<Form> getForms() {
        return forms;
    }

    public void setForms(List<Form> forms) {
        this.forms = forms;
    }
}
