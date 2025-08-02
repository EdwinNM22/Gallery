package com.proyecto.galeria.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "equipo_usuario")
@Data
@NoArgsConstructor
@AllArgsConstructor
@IdClass(EquipoUsuario.EquipoUsuarioId.class)
public class EquipoUsuario {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipo_id")
    private Equipo equipo;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private usuario usuario;

    @Column(name = "estado")
    private String estado;

    // Optional: still included if you need it later
    @Column(name = "created_at", insertable = false, updatable = false)
    private java.sql.Timestamp createdAt;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EquipoUsuarioId implements Serializable {
        private Integer equipo;
        private Integer usuario;
    }

    @ManyToOne
    @JoinColumn(name = "usuario_advertencia_id")
    private UsuarioAdvertencia usuarioAdvertencia;
}