package ec.edu.ups.ppw.rest;

import java.time.LocalDateTime;
import java.util.List;

import ec.edu.ups.ppw.dao.AsesoriaDAO;
import ec.edu.ups.ppw.dao.ProgramadorDAO;
import ec.edu.ups.ppw.dao.UsuarioDAO;
import ec.edu.ups.ppw.model.*;
import ec.edu.ups.ppw.rest.dto.AsesoriaRequest;
import ec.edu.ups.ppw.rest.dto.RechazoRequest;
import jakarta.ejb.EJB;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/asesorias")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AsesoriaResource {

    @EJB private AsesoriaDAO asesoriaDAO;
    @EJB private UsuarioDAO usuarioDAO;
    @EJB private ProgramadorDAO programadorDAO;

    @POST
    public Response solicitar(AsesoriaRequest req) {
        if (req == null || req.clienteId == null || req.programadorId == null || req.fechaInicio == null || req.fechaFin == null || req.modalidad == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Faltan campos").build();
        }

        Usuario cliente = usuarioDAO.read(req.clienteId);
        if (cliente == null) return Response.status(Response.Status.NOT_FOUND).entity("Cliente no existe").build();

        Programador prog = programadorDAO.read(req.programadorId);
        if (prog == null) return Response.status(Response.Status.NOT_FOUND).entity("Programador no existe").build();

        Asesoria a = new Asesoria();
        a.setCliente(cliente);
        a.setProgramador(prog);
        a.setFechaInicio(LocalDateTime.parse(req.fechaInicio));
        a.setFechaFin(LocalDateTime.parse(req.fechaFin));
        a.setModalidad(req.modalidad);
        a.setEstado(EstadoAsesoria.SOLICITADA);

        asesoriaDAO.insert(a);
        return Response.status(Response.Status.CREATED).entity(a).build();
    }

    @PUT
    @Path("/{id}/confirmar")
    public Response confirmar(@PathParam("id") Long id) {
        Asesoria a = asesoriaDAO.read(id);
        if (a == null) return Response.status(Response.Status.NOT_FOUND).build();

        a.setEstado(EstadoAsesoria.CONFIRMADA);
        asesoriaDAO.update(a);
        return Response.ok(a).build();
    }

    @PUT
    @Path("/{id}/rechazar")
    public Response rechazar(@PathParam("id") Long id, RechazoRequest req) {
        Asesoria a = asesoriaDAO.read(id);
        if (a == null) return Response.status(Response.Status.NOT_FOUND).build();

        a.setEstado(EstadoAsesoria.RECHAZADA);
        a.setMotivoRechazo(req == null ? null : req.motivo);
        asesoriaDAO.update(a);
        return Response.ok(a).build();
    }

    @GET
    @Path("/cliente/{clienteId}")
    public List<Asesoria> historialCliente(@PathParam("clienteId") Long clienteId) {
        return asesoriaDAO.listByCliente(clienteId);
    }

    @GET
    @Path("/programador/{programadorId}")
    public List<Asesoria> historialProgramador(@PathParam("programadorId") Long programadorId) {
        return asesoriaDAO.listByProgramador(programadorId);
    }
}
