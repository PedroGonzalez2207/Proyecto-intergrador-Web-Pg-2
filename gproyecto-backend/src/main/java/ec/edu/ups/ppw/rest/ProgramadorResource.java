package ec.edu.ups.ppw.rest;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;

import ec.edu.ups.ppw.dao.DisponibilidadDAO;
import ec.edu.ups.ppw.dao.ProgramadorDAO;
import ec.edu.ups.ppw.dao.ProyectoDAO;
import ec.edu.ups.ppw.model.Disponibilidad;
import ec.edu.ups.ppw.model.Programador;
import ec.edu.ups.ppw.model.Proyecto;
import ec.edu.ups.ppw.rest.dto.DisponibilidadRequest;
import ec.edu.ups.ppw.rest.dto.ProgramadorPublicDTO;
import ec.edu.ups.ppw.rest.dto.ProyectoPublicDTO;
import ec.edu.ups.ppw.rest.security.UserPrincipal;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.EJB;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;

@Path("/programadores")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ProgramadorResource {

    @EJB private ProgramadorDAO programadorDAO;
    @EJB private DisponibilidadDAO disponibilidadDAO;
    @EJB private ProyectoDAO proyectoDAO;

    @GET
    @RolesAllowed({ "Admin", "Programador", "Usuario" })
    public Response list() {
        List<Programador> list = programadorDAO.listActivos();

        List<ProgramadorPublicDTO> out = list.stream()
                .map(p -> {
                    List<Proyecto> proys = proyectoDAO.listByProgramador(p.getId());
                    List<ProyectoPublicDTO> dtoProys = proys.stream()
                            .map(pr -> new ProyectoPublicDTO(
                                    pr.getId(),
                                    pr.getCategoria(),
                                    pr.getNombre(),
                                    pr.getDescripcion(),
                                    pr.getParticipacion(),
                                    pr.getTecnologias(),
                                    pr.getRepoUrl(),
                                    pr.getDemoUrl()
                            ))
                            .collect(Collectors.toList());

                    return new ProgramadorPublicDTO(
                            p.getId(),
                            p.getUsuario() == null ? null : p.getUsuario().getId(),
                            nombreCompleto(p),
                            p.getBio(),
                            p.getEspecialidades(),
                            dtoProys
                    );
                })
                .collect(Collectors.toList());

        return Response.ok(out).build();
    }

    @GET
    @Path("/{id}")
    @RolesAllowed({ "Admin", "Programador", "Usuario" })
    public Response detalle(@PathParam("id") Long id) {
        Programador p = programadorDAO.readActivoWithUsuario(id);
        if (p == null) return Response.status(Response.Status.NOT_FOUND).entity("Programador no existe").build();

        List<Proyecto> proys = proyectoDAO.listByProgramador(p.getId());
        List<ProyectoPublicDTO> dtoProys = proys.stream()
                .map(pr -> new ProyectoPublicDTO(
                        pr.getId(),
                        pr.getCategoria(),
                        pr.getNombre(),
                        pr.getDescripcion(),
                        pr.getParticipacion(),
                        pr.getTecnologias(),
                        pr.getRepoUrl(),
                        pr.getDemoUrl()
                ))
                .collect(Collectors.toList());

        ProgramadorPublicDTO out = new ProgramadorPublicDTO(
                p.getId(),
                p.getUsuario() == null ? null : p.getUsuario().getId(),
                nombreCompleto(p),
                p.getBio(),
                p.getEspecialidades(),
                dtoProys
        );

        return Response.ok(out).build();
    }

    @GET
    @Path("/me")
    @RolesAllowed({ "Admin", "Programador" })
    public Response me(@Context SecurityContext sc) {
        UserPrincipal up = principal(sc);
        if (up == null) return Response.status(Response.Status.UNAUTHORIZED).entity("Unauthorized").build();

        Programador p = programadorDAO.findActivoByUsuarioFirebaseUid(up.getUid());
        if (p == null) return Response.status(Response.Status.NOT_FOUND).entity("No existe Programador asociado").build();

        return Response.ok(p.getId()).build();
    }

    @GET
    @Path("/{id}/disponibilidades")
    @RolesAllowed({ "Admin", "Programador", "Usuario" })
    public Response disponibilidades(@PathParam("id") Long programadorId, @QueryParam("fecha") String fecha) {
        if (programadorId == null) return Response.status(Response.Status.BAD_REQUEST).entity("Id inválido").build();

        Programador p = programadorDAO.readActivoWithUsuario(programadorId);
        if (p == null) return Response.status(Response.Status.NOT_FOUND).entity("Programador no existe").build();

        if (fecha != null && !fecha.isBlank()) {
            LocalDate f;
            try { f = LocalDate.parse(fecha.trim()); }
            catch (DateTimeParseException e) { return Response.status(Response.Status.BAD_REQUEST).entity("Formato fecha inválido").build(); }

            List<Disponibilidad> list = disponibilidadDAO.listByProgramadorAndFecha(programadorId, f);
            return Response.ok(list).build();
        }

        List<Disponibilidad> list = disponibilidadDAO.listByProgramador(programadorId);
        return Response.ok(list).build();
    }

    @POST
    @Path("/{id}/disponibilidades")
    @RolesAllowed({ "Admin" })
    public Response crearDisponibilidad(@PathParam("id") Long programadorId, DisponibilidadRequest req) {
        if (programadorId == null) return Response.status(Response.Status.BAD_REQUEST).entity("Id inválido").build();

        Programador p = programadorDAO.readActivoWithUsuario(programadorId);
        if (p == null) return Response.status(Response.Status.NOT_FOUND).entity("Programador no existe").build();

        if (req == null || req.fecha == null || req.horaInicio == null || req.horaFin == null || req.modalidad == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Faltan campos").build();
        }

        LocalDate f;
        try { f = LocalDate.parse(req.fecha.trim()); }
        catch (DateTimeParseException e) { return Response.status(Response.Status.BAD_REQUEST).entity("Formato fecha inválido").build(); }

        LocalTime inicio;
        LocalTime fin;
        try {
            inicio = LocalTime.parse(req.horaInicio);
            fin = LocalTime.parse(req.horaFin);
        } catch (DateTimeParseException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Formato hora inválido").build();
        }

        if (!inicio.isBefore(fin)) {
            return Response.status(Response.Status.BAD_REQUEST).entity("horaInicio debe ser menor que horaFin").build();
        }

        if (disponibilidadDAO.existsOverlappingFecha(programadorId, f, inicio, fin)) {
            return Response.status(Response.Status.CONFLICT).entity("Horario se cruza").build();
        }

        Disponibilidad d = new Disponibilidad();
        d.setProgramador(p);
        d.setFecha(f);
        d.setDiaSemana(null);
        d.setHoraInicio(inicio);
        d.setHoraFin(fin);
        d.setModalidad(req.modalidad);
        d.setActivo(true);

        disponibilidadDAO.insert(d);
        return Response.status(Response.Status.CREATED).entity(d).build();
    }

    @DELETE
    @Path("/{id}/disponibilidades/{dispId}")
    @RolesAllowed({ "Admin" })
    public Response eliminarDisponibilidad(@PathParam("id") Long programadorId, @PathParam("dispId") Long dispId) {
        Programador p = programadorDAO.readActivoWithUsuario(programadorId);
        if (p == null) return Response.status(Response.Status.NOT_FOUND).entity("Programador no existe").build();

        boolean ok = disponibilidadDAO.softDelete(dispId);
        if (!ok) return Response.status(Response.Status.NOT_FOUND).entity("Disponibilidad no existe").build();

        return Response.noContent().build();
    }

    private UserPrincipal principal(SecurityContext sc) {
        if (sc == null || sc.getUserPrincipal() == null) return null;
        return (sc.getUserPrincipal() instanceof UserPrincipal up) ? up : null;
    }

    private String nombreCompleto(Programador p) {
        if (p == null || p.getUsuario() == null) return "";

        String n = p.getUsuario().getNombres() == null ? "" : p.getUsuario().getNombres().trim();
        String a = p.getUsuario().getApellidos() == null ? "" : p.getUsuario().getApellidos().trim();
        String full = (n + " " + a).trim();

        if (!full.isBlank() && !full.equalsIgnoreCase("usuario")) return full;

        String email = p.getUsuario().getEmail() == null ? "" : p.getUsuario().getEmail().trim();
        if (!email.isBlank()) return email;

        String uid = p.getUsuario().getFirebaseUid() == null ? "" : p.getUsuario().getFirebaseUid().trim();
        return uid;
    }
}
