package com.proyecto.galeria.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Table(name = "medicion")
public class Medicion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String etiqueta1;
    private double punto1X;
    private double punto1Y;

    private String etiqueta2;
    private double punto2X;
    private double punto2Y;

    private double distanciaMetros;
    private String colorHex;

    @ManyToOne
    @JoinColumn(name = "plano_id")
    @JsonBackReference(value = "plano-medicion")
    private Plano plano;
}
