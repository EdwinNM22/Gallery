package com.proyecto.galeria.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Table(name = "plano")
public class Plano {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre_imagen", nullable = false)
    private String nombreImagen;

    @Column(nullable = false, length = 500)
    private String url;

    @Column(length = 500)
    private String comentario;

    private Double escala;
    @Column(name = "total_metros")
    private Double totalMetros;


    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    @JsonBackReference(value = "usuario-plano")
    private usuario usuario;

    @OneToMany(mappedBy = "plano", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference(value = "plano-medicion")
    private List<Medicion> mediciones = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "proyecto_plano_id")
    @JsonBackReference(value = "proyectoPlano-plano")
    private ProyectoPlano proyectoPlano;
}
