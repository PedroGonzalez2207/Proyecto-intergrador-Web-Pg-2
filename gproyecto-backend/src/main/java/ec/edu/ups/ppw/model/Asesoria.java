package ec.edu.ups.ppw.model;

import java.time.LocalDateTime;
import jakarta.persistence.*;

@Entity
@Table(name = "TBL_ASESORIA")
public class Asesoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // usuario cliente
    @ManyToOne(optional=false)
    @JoinColumn(name="cliente_id", nullable=false,
        foreignKey=@ForeignKey(name="FK_ASE_CLIENTE"))
    private Usuario cliente;

    @ManyToOne(optional=false)
    @JoinColumn(name="programador_id", nullable=false,
        foreignKey=@ForeignKey(name="FK_ASE_PROG"))
    private Programador programador;

    @Column(name="fecha_inicio", nullable=false)
    private LocalDateTime fechaInicio;

    @Column(name="fecha_fin", nullable=false)
    private LocalDateTime fechaFin;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false, length=15)
    private Modalidad modalidad;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false, length=20)
    private EstadoAsesoria estado = EstadoAsesoria.SOLICITADA;

    @Column(name="motivo_rechazo", length=300)
    private String motivoRechazo;

    @Column(name="created_at", nullable=false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public Asesoria() {
    	
    }

    public Long getId() { 
    	return id; 
    }

    public Usuario getCliente() { 
    	return cliente; 
    }
    
    public void setCliente(Usuario cliente) { 
    	this.cliente = cliente; 
    }

    public Programador getProgramador() { 
    	return programador;
    }
    
    public void setProgramador(Programador programador) { 
    	this.programador = programador; 
    }

    public LocalDateTime getFechaInicio() { 
    	return fechaInicio; 
    }
    
    public void setFechaInicio(LocalDateTime fechaInicio) { 
    	this.fechaInicio = fechaInicio; 
    }

    public LocalDateTime getFechaFin() { 
    	return fechaFin; 
    }
    
    public void setFechaFin(LocalDateTime fechaFin) { 
    	this.fechaFin = fechaFin; 
    }

    public Modalidad getModalidad() { 
    	return modalidad; 
    }
    
    public void setModalidad(Modalidad modalidad) { 
    	this.modalidad = modalidad; 
    }

    public EstadoAsesoria getEstado() { 
    	return estado; 
    }
    
    public void setEstado(EstadoAsesoria estado) { 
    	this.estado = estado; 
    }

    public String getMotivoRechazo() { 
    	return motivoRechazo; 
    }
    
    public void setMotivoRechazo(String motivoRechazo) { 
    	this.motivoRechazo = motivoRechazo; 
    }

    public LocalDateTime getCreatedAt() { 
    	return createdAt; 
    }
}
