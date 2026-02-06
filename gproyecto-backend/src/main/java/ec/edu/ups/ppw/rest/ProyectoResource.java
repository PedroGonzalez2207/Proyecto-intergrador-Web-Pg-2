package ec.edu.ups.ppw.rest;

import java.util.List;

import ec.edu.ups.ppw.dao.ProgramadorDAO;
import ec.edu.ups.ppw.dao.ProyectoDAO;
import ec.edu.ups.ppw.model.Programador;
import ec.edu.ups.ppw.model.Proyecto;
import ec.edu.ups.ppw.rest.dto.ProyectoRequest;
import ec.edu.ups.ppw.rest.security.UserPrincipal;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.EJB;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;

@Path("/proyectos")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ProyectoResource {

    @EJB private ProyectoDAO proyectoDAO;
    @EJB private ProgramadorDAO programadorDAO;

    @GET
    @Path("/programador/{programadorId}")
    @RolesAllowed({ "Admin", "Programador", "Usuario" })
    public Response listByProgramador(@PathParam("programadorId") Long programadorId) {
        if (programadorId == null) return Response.status(Response.Status.BAD_REQUEST).entity("Id inválido").build();

        Programador p = programadorDAO.read(programadorId);
        if (p == null) return Response.status(Response.Status.NOT_FOUND).entity("Programador no existe").build();

        List<Proyecto> list = proyectoDAO.listByProgramador(programadorId);
        return Response.ok(list).build();
    }

    @GET
    @Path("/me")
    @RolesAllowed({ "Admin", "Programador" })
    public Response listMine(@Context SecurityContext sc) {
        UserPrincipal up = principal(sc);
        if (up == null) return Response.status(Response.Status.UNAUTHORIZED).entity("Unauthorized").build();

        Programador p = programadorDAO.findByUsuarioFirebaseUid(up.getUid());
        if (p == null) return Response.status(Response.Status.NOT_FOUND).entity("No existe Programador asociado").build();

        List<Proyecto> list = proyectoDAO.listByProgramador(p.getId());
        return Response.ok(list).build();
    }

    @POST
    @Path("/me")
    @RolesAllowed({ "Admin", "Programador" })
    public Response crearMine(ProyectoRequest req, @Context SecurityContext sc) {
        UserPrincipal up = principal(sc);
        if (up == null) return Response.status(Response.Status.UNAUTHORIZED).entity("Unauthorized").build();

        Programador p = programadorDAO.findByUsuarioFirebaseUid(up.getUid());
        if (p == null) return Response.status(Response.Status.NOT_FOUND).entity("No existe Programador asociado").build();

        if (req == null || req.categoria == null || req.participacion == null || req.nombre == null || req.nombre.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Faltan campos").build();
        }

        Proyecto pr = new Proyecto();
        pr.setProgramador(p);
        pr.setCategoria(req.categoria);
        pr.setNombre(req.nombre.trim());
        pr.setDescripcion(req.descripcion == null ? "" : req.descripcion.trim());
        pr.setParticipacion(req.participacion);
        pr.setTecnologias(req.tecnologias == null ? "" : req.tecnologias.trim());
        pr.setRepoUrl(req.repoUrl);
        pr.setDemoUrl(req.demoUrl);
        pr.setActivo(true);

        proyectoDAO.insert(pr);
        return Response.status(Response.Status.CREATED).entity(pr).build();
    }

    @PUT
    @Path("/{id}")
    @RolesAllowed({ "Admin", "Programador" })
    public Response actualizar(@PathParam("id") Long id, ProyectoRequest req, @Context SecurityContext sc) {
        UserPrincipal up = principal(sc);
        if (up == null) return Response.status(Response.Status.UNAUTHORIZED).entity("Unauthorized").build();

        Proyecto pr = proyectoDAO.read(id);
        if (pr == null) return Response.status(Response.Status.NOT_FOUND).entity("Proyecto no existe").build();

        if (sc.isUserInRole("Programador")) {
            Programador me = programadorDAO.findByUsuarioFirebaseUid(up.getUid());
            if (me == null || pr.getProgramador() == null || !pr.getProgramador().getId().equals(me.getId())) {
                return Response.status(Response.Status.FORBIDDEN).entity("No autorizado").build();
            }
        }

        if (req == null) return Response.status(Response.Status.BAD_REQUEST).entity("Body requerido").build();
        if (req.categoria != null) pr.setCategoria(req.categoria);
        if (req.participacion != null) pr.setParticipacion(req.participacion);
        if (req.nombre != null && !req.nombre.isBlank()) pr.setNombre(req.nombre.trim());
        if (req.descripcion != null) pr.setDescripcion(req.descripcion.trim());
        if (req.tecnologias != null) pr.setTecnologias(req.tecnologias.trim());
        if (req.repoUrl != null) pr.setRepoUrl(req.repoUrl);
        if (req.demoUrl != null) pr.setDemoUrl(req.demoUrl);

        proyectoDAO.update(pr);
        return Response.ok(pr).build();
    }

    @DELETE
    @Path("/{id}")
    @RolesAllowed({ "Admin", "Programador" })
    public Response eliminar(@PathParam("id") Long id, @Context SecurityContext sc) {
        UserPrincipal up = principal(sc);
        if (up == null) return Response.status(Response.Status.UNAUTHORIZED).entity("Unauthorized").build();

        Proyecto pr = proyectoDAO.read(id);
        if (pr == null) return Response.status(Response.Status.NOT_FOUND).entity("Proyecto no existe").build();

        if (sc.isUserInRole("Programador")) {
            Programador me = programadorDAO.findByUsuarioFirebaseUid(up.getUid());
            if (me == null || pr.getProgramador() == null || !pr.getProgramador().getId().equals(me.getId())) {
                return Response.status(Response.Status.FORBIDDEN).entity("No autorizado").build();
            }
        }

        boolean ok = proyectoDAO.softDelete(id);
        if (!ok) return Response.status(Response.Status.NOT_FOUND).entity("Proyecto no existe").build();

        return Response.noContent().build();
    }

    private UserPrincipal principal(SecurityContext sc) {
        if (sc == null || sc.getUserPrincipal() == null) return null;
        return (sc.getUserPrincipal() instanceof UserPrincipal up) ? up : null;
    }
}
