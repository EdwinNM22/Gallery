package com.proyecto.galeria.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "equipos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Equipo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String nombre;

    private String descripcion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "album_id")
    private album proyecto;

    @OneToMany(mappedBy = "equipo", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EquipoUsuario> miembros;

    // Helper methods for safe collection modification
    public void addMiembro(EquipoUsuario miembro) {
        this.miembros.add(miembro);
        miembro.setEquipo(this);
    }

    public void removeMiembro(EquipoUsuario miembro) {
        this.miembros.remove(miembro);
        miembro.setEquipo(null);
    }
}
