package com.proyecto.galeria.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "form")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Form {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "nombre_cliente")
    private String nombreCliente;

    private String direccion;

    @Column(name = "tipo_edificio")
    private String tipoEdificio;

    @Column(name = "fecha_evaluacion")
    private LocalDate fechaEvaluacion;

    @Column(name = "nombre_evaluador")
    private String nombreEvaluador;

    @Column(name = "persona_contacto")
    private String personaContacto;

    @Column(name = "mantenimiento_historia")
    private Boolean mantenimientoHistoria;

    @Column(name = "fecha_ultimo_mantenimiento")
    private LocalDate fechaUltimoMantenimiento;

    @Column(name = "tipo_sistema")
    private String tipoSistema;

    private String accesibilidad;

    @Column(name = "puertas_acceso_existentes")
    private String puertasAccesoExistentes;

    @Column(name = "puertas_acceso_a_anadir")
    private String puertasAccesoAAnadir;

    @Column(name = "conductos_visibles")
    private Boolean conductosVisibles;

    @Column(name = "tipo_material")
    private String tipoMaterial;

    @Column(name = "acumulacion_polvillo")
    private Boolean acumulacionPolvillo;

    @Column(name = "acumulacion_polvillo_comentario")
    @Lob
    private String acumulacionPolvilloComentario;

    @Column(name = "escombros_visibles")
    private Boolean escombrosVisibles;

    @Column(name = "escombros_visibles_comentario")
    @Lob
    private String escombrosVisiblesComentario;

    private Boolean moho;

    @Column(name = "moho_comentario")
    @Lob
    private String mohoComentario;

    @Column(name = "roedores_insectos")
    private Boolean roedoresInsectos;

    @Column(name = "roedores_insectos_comentario")
    @Lob
    private String roedoresInsectosComentario;

    @Column(name = "grasa_hotte")
    private Boolean grasaHotte;

    @Column(name = "grasa_hotte_comentario")
    @Lob
    private String grasaHotteComentario;

    @Column(name = "olores_sospechosos")
    private Boolean oloresSospechosos;

    @Column(name = "olores_sospechosos_comentario")
    @Lob
    private String oloresSospechososComentario;

    @Column(name = "estado_rejillas")
    private String estadoRejillas;

    @Column(name = "estado_motor")
    private String estadoMotor;

    @Column(name = "estado_filtros")
    private String estadoFiltros;

    @Column(name = "cantidad_conductos")
    private Integer cantidadConductos;

    @Column(name = "longitud_estimacion")
    private String longitudEstimacion;

    @Column(name = "cantidad_codos")
    private Integer cantidadCodos;

    @Column(name = "cantidad_salidas")
    private Integer cantidadSalidas;

    @Column(name = "altura_trabajo")
    private String alturaTrabajo;

    @Lob
    private String observaciones;

    @Column(name = "limpieza_completa")
    private Boolean limpiezaCompleta;

    @Column(name = "instalar_puertas_acceso")
    private Boolean instalarPuertasAcceso;

    @Column(name = "reemplazo_filtros")
    private Boolean reemplazoFiltros;

    @Column(name = "productos_recomendados")
    @Lob
    private String productosRecomendados;

    @Column(name = "futuro")
    private Boolean futuro;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private usuario usuario;

    @ManyToOne
    @JsonManagedReference
    @JoinColumn(name = "expediente_id")
    private Expediente expediente;
}