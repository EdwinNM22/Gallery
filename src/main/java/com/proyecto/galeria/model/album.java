package com.proyecto.galeria.model;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
@Table(name = "albumes")
public class album {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String nombre;
    private String descripcion;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fechaInicio;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fechaFin;
    private String ubicacion;
    private String contacto;
    private String claveAlarma;
    private String datosAdicionales;
    private Double horasPorProyecto;
    private Double precioHora;
    private String estado = "pendiente"; // Valor por defecto

    private String notas;

    @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
    private LocalTime horaInicio;
    @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
    private LocalTime horaFin;

    @OneToMany(mappedBy = "album", cascade = {CascadeType.PERSIST, CascadeType.MERGE},
            orphanRemoval = false, fetch = FetchType.LAZY) // Añadido FetchType.LAZY
    private List<SubAlbum> subAlbumes = new ArrayList<>();

    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY) // Añadido FetchType.LAZY
    @JoinTable(
            name = "album_subalbum",
            joinColumns = @JoinColumn(name = "album_id"),
            inverseJoinColumns = @JoinColumn(name = "subalbum_id")
    )
    private List<foto> fotos;

    @ManyToOne(cascade = CascadeType.PERSIST)
    private usuario usuario;

    // Métodos seguros para manejar subAlbumes (AÑADIDOS)
    public void addSubAlbum(SubAlbum subAlbum) {
        subAlbumes.add(subAlbum);
        subAlbum.setAlbum(this);
    }

    public void removeSubAlbum(SubAlbum subAlbum) {
        subAlbumes.remove(subAlbum);
        subAlbum.setAlbum(null);
    }

    // Getter modificado para devolver lista inmodificable (AÑADIDO)
    public List<SubAlbum> getSubAlbumes() {
        return Collections.unmodifiableList(subAlbumes);
    }

    // Setter seguro para subAlbumes (AÑADIDO)
    public void setSubAlbumes(List<SubAlbum> subAlbumes) {
        this.subAlbumes.clear();
        if (subAlbumes != null) {
            this.subAlbumes.addAll(subAlbumes);
            this.subAlbumes.forEach(sa -> sa.setAlbum(this));
        }
    }


    //Nuevos para los equipos

    @OneToMany(mappedBy = "proyecto", fetch = FetchType.LAZY)
    private List<Equipo> equipos = new ArrayList<>();



    // Métodos para manejar equipos
    public void addEquipo(Equipo equipo) {
        equipos.add(equipo);
        equipo.setProyecto(this);
    }

    public void removeEquipo(Equipo equipo) {
        equipos.remove(equipo);
        equipo.setProyecto(null);
    }

    public List<Equipo> getEquipos() {
        return Collections.unmodifiableList(equipos);
    }
    public album() {}


    public album(Integer id, String nombre, String descripcion, LocalDate fechaInicio, LocalDate fechaFin, String ubicacion, String contacto, String claveAlarma, String datosAdicionales, Double horasPorProyecto, Double precioHora, String estado, String notas, LocalTime horaInicio, LocalTime horaFin) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.ubicacion = ubicacion;
        this.contacto = contacto;
        this.claveAlarma = claveAlarma;
        this.datosAdicionales = datosAdicionales;
        this.horasPorProyecto = horasPorProyecto;
        this.precioHora = precioHora;
        this.estado = estado;
        this.notas = notas;
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
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

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDate fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public LocalDate getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(LocalDate fechaFin) {
        this.fechaFin = fechaFin;
    }

    public String getUbicacion() {
        return ubicacion;
    }

    public void setUbicacion(String ubicacion) {
        this.ubicacion = ubicacion;
    }

    public String getContacto() {
        return contacto;
    }

    public void setContacto(String contacto) {
        this.contacto = contacto;
    }

    public String getClaveAlarma() {
        return claveAlarma;
    }

    public void setClaveAlarma(String claveAlarma) {
        this.claveAlarma = claveAlarma;
    }

    public String getDatosAdicionales() {
        return datosAdicionales;
    }

    public void setDatosAdicionales(String datosAdicionales) {
        this.datosAdicionales = datosAdicionales;
    }

    public Double getHorasPorProyecto() {
        return horasPorProyecto;
    }

    public void setHorasPorProyecto(Double horasPorProyecto) {
        this.horasPorProyecto = horasPorProyecto;
    }

    public Double getPrecioHora() {
        return precioHora;
    }

    public void setPrecioHora(Double precioHora) {
        this.precioHora = precioHora;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getNotas() {
        return notas;
    }

    public void setNotas(String notas) {
        this.notas = notas;
    }

    public LocalTime getHoraInicio() {
        return horaInicio;
    }

    public void setHoraInicio(LocalTime horaInicio) {
        this.horaInicio = horaInicio;
    }

    public LocalTime getHoraFin() {
        return horaFin;
    }

    public void setHoraFin(LocalTime horaFin) {
        this.horaFin = horaFin;
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

    public void setEquipos(List<Equipo> equipos) {
        this.equipos = equipos;
    }
}