package com.proyecto.galeria.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "fotos")
public class foto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String nombre;
    private String descripcion;
    private String imagen;
    private Date fecha;

    @ManyToOne
    private usuario usuario;

    @ManyToMany(mappedBy = "fotos")
    private List<album> albumes;

    // Relación ManyToOne con SubAlbum
    @ManyToOne
    private SubAlbum subAlbum;




}