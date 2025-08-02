package com.proyecto.galeria.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "usuario_advertencia")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioAdvertencia {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private usuario usuario;

    @ManyToOne
    @JoinColumn(name = "advertencia_tipo_id")
    private AdvertenciaTipo advertenciaTipo;
}
