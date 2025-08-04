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
@Table(name = "proyecto_plano")
public class ProyectoPlano {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(length = 500)
    private String descripcion;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    @JsonBackReference(value = "usuario-proyectoPlano")
    private usuario usuario;

    @OneToMany(mappedBy = "proyectoPlano", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference(value = "proyectoPlano-plano")
    private List<Plano> planos = new ArrayList<>();
}