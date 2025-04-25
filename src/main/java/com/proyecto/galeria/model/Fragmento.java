package com.proyecto.galeria.model;

import java.util.List;


public class Fragmento {
    private String nombre;
    private String descripcion;
    private List<SubAlbum> subAlbumes;


    public Fragmento() {

    }

    public Fragmento(String nombre, String descripcion, List<SubAlbum> subAlbumes) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.subAlbumes = subAlbumes;
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

    public List<SubAlbum> getSubAlbumes() {
        return subAlbumes;
    }

    public void setSubAlbumes(List<SubAlbum> subAlbumes) {
        this.subAlbumes = subAlbumes;
    }

    @Override
    public String toString() {
        return "Fragmento{" +
                "nombre='" + nombre + '\'' +
                ", descripcion='" + descripcion + '\'' +
                ", subAlbumes=" + subAlbumes +
                '}';
    }
}
