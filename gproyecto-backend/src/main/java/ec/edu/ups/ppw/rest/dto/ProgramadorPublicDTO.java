package ec.edu.ups.ppw.rest.dto;

import java.util.List;

public class ProgramadorPublicDTO {
    public Long id;
    public Long usuarioId;
    public String nombre;
    public String bio;
    public String especialidades;
    public List<ProyectoPublicDTO> proyectos;

    public ProgramadorPublicDTO() {}

    public ProgramadorPublicDTO(Long id, Long usuarioId, String nombre, String bio, String especialidades, List<ProyectoPublicDTO> proyectos) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.nombre = nombre;
        this.bio = bio;
        this.especialidades = especialidades;
        this.proyectos = proyectos;
    }
}
