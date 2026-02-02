package ec.edu.ups.ppw.rest;

import java.time.LocalTime;
import java.util.List;

import ec.edu.ups.ppw.dao.DisponibilidadDAO;
import ec.edu.ups.ppw.dao.ProgramadorDAO;
import ec.edu.ups.ppw.model.Disponibilidad;
import ec.edu.ups.ppw.model.Programador;
import ec.edu.ups.ppw.rest.dto.DisponibilidadRequest;
import jakarta.ejb.EJB;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/programadores")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ProgramadorResource {

    @EJB
    private ProgramadorDAO programadorDAO;

    @EJB
    private DisponibilidadDAO disponibilidadDAO;

    @GET
    @Path("/{id}/disponibilidades")
    public List<Disponibilidad> listarDisponibilidades(@PathParam("id") Long programadorId) {
        return disponibilidadDAO.listByProgramador(programadorId);
    }

    @POST
    @Path("/{id}/disponibilidades")
    public Response crearDisponibilidad(@PathParam("id") Long programadorId, DisponibilidadRequest req) {
        Programador p = programadorDAO.read(programadorId);
        if (p == null) return Response.status(Response.Status.NOT_FOUND).entity("Programador no existe").build();

        if (req == null || req.diaSemana == null || req.horaInicio == null || req.horaFin == null || req.modalidad == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Faltan campos").build();
        }

        Disponibilidad d = new Disponibilidad();
        d.setProgramador(p);
        d.setDiaSemana(req.diaSemana);
        d.setHoraInicio(LocalTime.parse(req.horaInicio));
        d.setHoraFin(LocalTime.parse(req.horaFin));
        d.setModalidad(req.modalidad);
        d.setActivo(true);

        disponibilidadDAO.insert(d);
        return Response.status(Response.Status.CREATED).entity(d).build();
    }
}
