package ec.edu.ups.ppw.model;

import jakarta.persistence.*;

@Entity
@Table(name = "TBL_PROGRAMADOR")
public class Programador {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional=false)
    @JoinColumn(name="usuario_id", nullable=false, unique=true,
        foreignKey=@ForeignKey(name="FK_PROG_USUARIO"))
    private Usuario usuario;

    @Column(length=500)
    private String bio;

    @Column(length=300)
    private String especialidades;

    public Programador() {
    	
    }

    public Long getId() { 
    	return id; 
    }

    public Usuario getUsuario() { 
    	return usuario; 
    }
    
    public void setUsuario(Usuario usuario) { 
    	this.usuario = usuario; 
    }

    public String getBio() { 
    	return bio; 
    }
    
    public void setBio(String bio) 
    { 
    	this.bio = bio; 
    }

    public String getEspecialidades() { 
    	return especialidades; 
    }
    
    public void setEspecialidades(String especialidades) { 
    	this.especialidades = especialidades; 
    }
}
