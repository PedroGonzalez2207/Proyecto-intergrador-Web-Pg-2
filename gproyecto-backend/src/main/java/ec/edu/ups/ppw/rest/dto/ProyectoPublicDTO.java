package ec.edu.ups.ppw.rest.dto;

import ec.edu.ups.ppw.model.CategoriaProyecto;
import ec.edu.ups.ppw.model.TipoParticipacion;

public class ProyectoPublicDTO {
    public Long id;
    public CategoriaProyecto categoria;
    public String nombre;
    public String descripcion;
    public TipoParticipacion participacion;
    public String tecnologias;
    public String repoUrl;
    public String demoUrl;

    public ProyectoPublicDTO() {}

    public ProyectoPublicDTO(Long id, CategoriaProyecto categoria, String nombre, String descripcion,
                             TipoParticipacion participacion, String tecnologias, String repoUrl, String demoUrl) {
        this.id = id;
        this.categoria = categoria;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.participacion = participacion;
        this.tecnologias = tecnologias;
        this.repoUrl = repoUrl;
        this.demoUrl = demoUrl;
    }
}
