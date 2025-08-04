package com.proyecto.galeria.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
@Entity
@Table(name = "usuarios")
public class usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    private Integer id;

    @Column(nullable = false, unique = true)
    private String nombre;

    private String email ;
    private String tipo_usuario;
    private String password;

    @Column(nullable = true, unique = true)
    private String username;

    private String passwordSinEncriptar;


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
    @JsonManagedReference(value = "usuario-expediente")
    private List<Expediente> expedientes;


    @OneToMany(mappedBy = "usuario")
    private List<Form> forms;

    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference(value = "usuario-proyectoPlano")
    private List<ProyectoPlano> proyectos = new ArrayList<>();



    public usuario() {}

    public usuario(Integer id, String nombre, String email, String tipo_usuario, String password, String username, String passwordSinEncriptar) {
        this.id = id;
        this.nombre = nombre;
        this.email = email;
        this.tipo_usuario = tipo_usuario;
        this.password = password;
        this.username = username;
        this.passwordSinEncriptar = passwordSinEncriptar;
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

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordSinEncriptar() {
        return passwordSinEncriptar;
    }

    public void setPasswordSinEncriptar(String passwordSinEncriptar) {
        this.passwordSinEncriptar = passwordSinEncriptar;
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

    public List<ProyectoPlano> getProyectos() {
        return proyectos;
    }

    public void setProyectos(List<ProyectoPlano> proyectos) {
        this.proyectos = proyectos;
    }

    public List<UsuarioAdvertencia> getAdvertencias() {
        return advertencias;
    }

    public void setAdvertencias(List<UsuarioAdvertencia> advertencias) {
        this.advertencias = advertencias;
    }

    @OneToMany(mappedBy = "usuario")
    private List<UsuarioAdvertencia> advertencias;
}
