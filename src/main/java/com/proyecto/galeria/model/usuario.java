package com.proyecto.galeria.model;



import javax.persistence.*;
import java.util.List;

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

    @OneToMany(mappedBy = "usuario")
    private List<foto> fotos;

    @OneToMany(mappedBy = "usuario", cascade = CascadeType.PERSIST) // O CascadeType.MERGE según el comportamiento deseado
    private List<album> albumes;


    public usuario() {

    }

    public usuario(Integer id, String nombre, String email, String tipo_usuario, String password ) {
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
}
