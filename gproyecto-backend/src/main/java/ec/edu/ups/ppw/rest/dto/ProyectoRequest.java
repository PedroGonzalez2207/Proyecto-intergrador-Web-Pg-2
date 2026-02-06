package ec.edu.ups.ppw.rest.dto;

import ec.edu.ups.ppw.model.CategoriaProyecto;
import ec.edu.ups.ppw.model.TipoParticipacion;

public class ProyectoRequest {
    public CategoriaProyecto categoria;
    public String nombre;
    public String descripcion;
    public TipoParticipacion participacion;
    public String tecnologias;
    public String repoUrl;
    public String demoUrl;
}
