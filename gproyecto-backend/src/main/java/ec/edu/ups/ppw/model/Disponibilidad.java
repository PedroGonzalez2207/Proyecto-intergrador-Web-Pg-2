package ec.edu.ups.ppw.model;

import java.time.LocalDate;
import java.time.LocalTime;

import jakarta.json.bind.annotation.JsonbTransient;
import jakarta.persistence.*;

@Entity
@Table(name = "TBL_DISPONIBILIDAD")
public class Disponibilidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonbTransient
    @ManyToOne(optional = false)
    @JoinColumn(name = "programador_id", nullable = false,
            foreignKey = @ForeignKey(name = "FK_DISP_PROG"))
    private Programador programador;

    @Column(name = "fecha")
    private LocalDate fecha;

    @Column(name = "dia_semana")
    private Integer diaSemana;

    @Column(name = "hora_inicio", nullable = false)
    private LocalTime horaInicio;

    @Column(name = "hora_fin", nullable = false)
    private LocalTime horaFin;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private Modalidad modalidad;

    @Column(nullable = false)
    private Boolean activo = true;

    public Disponibilidad() {}

    public Long getId() { return id; }

    public Programador getProgramador() { return programador; }
    public void setProgramador(Programador programador) { this.programador = programador; }

    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }

    public Integer getDiaSemana() { return diaSemana; }
    public void setDiaSemana(Integer diaSemana) { this.diaSemana = diaSemana; }

    public LocalTime getHoraInicio() { return horaInicio; }
    public void setHoraInicio(LocalTime horaInicio) { this.horaInicio = horaInicio; }

    public LocalTime getHoraFin() { return horaFin; }
    public void setHoraFin(LocalTime horaFin) { this.horaFin = horaFin; }

    public Modalidad getModalidad() { return modalidad; }
    public void setModalidad(Modalidad modalidad) { this.modalidad = modalidad; }

    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }
}
