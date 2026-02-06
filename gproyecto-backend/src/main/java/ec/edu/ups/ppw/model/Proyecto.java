package ec.edu.ups.ppw.model;

import jakarta.json.bind.annotation.JsonbTransient;
import jakarta.persistence.*;

@Entity
@Table(name = "TBL_PROYECTO")
public class Proyecto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonbTransient
    @ManyToOne(optional = false)
    @JoinColumn(name = "programador_id", nullable = false,
            foreignKey = @ForeignKey(name = "FK_PROY_PROG"))
    private Programador programador;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CategoriaProyecto categoria;

    @Column(nullable = false, length = 120)
    private String nombre;

    @Column(length = 800)
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(name = "participacion", nullable = false, length = 20)
    private TipoParticipacion participacion;

    @Column(length = 300)
    private String tecnologias;

    @Column(name = "repo_url", length = 400)
    private String repoUrl;

    @Column(name = "demo_url", length = 400)
    private String demoUrl;

    @Column(nullable = false)
    private Boolean activo = true;

    public Proyecto() {}

    public Long getId() { return id; }

    public Programador getProgramador() { return programador; }
    public void setProgramador(Programador programador) { this.programador = programador; }

    public CategoriaProyecto getCategoria() { return categoria; }
    public void setCategoria(CategoriaProyecto categoria) { this.categoria = categoria; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public TipoParticipacion getParticipacion() { return participacion; }
    public void setParticipacion(TipoParticipacion participacion) { this.participacion = participacion; }

    public String getTecnologias() { return tecnologias; }
    public void setTecnologias(String tecnologias) { this.tecnologias = tecnologias; }

    public String getRepoUrl() { return repoUrl; }
    public void setRepoUrl(String repoUrl) { this.repoUrl = repoUrl; }

    public String getDemoUrl() { return demoUrl; }
    public void setDemoUrl(String demoUrl) { this.demoUrl = demoUrl; }

    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }
}
