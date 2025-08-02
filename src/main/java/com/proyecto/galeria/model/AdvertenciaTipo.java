package com.proyecto.galeria.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "advertencia_tipo")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdvertenciaTipo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    private String name;
}
