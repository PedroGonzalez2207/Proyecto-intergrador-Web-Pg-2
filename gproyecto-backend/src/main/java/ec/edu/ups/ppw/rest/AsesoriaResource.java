package ec.edu.ups.ppw.rest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;

import ec.edu.ups.ppw.dao.AsesoriaDAO;
import ec.edu.ups.ppw.dao.DisponibilidadDAO;
import ec.edu.ups.ppw.dao.ProgramadorDAO;
import ec.edu.ups.ppw.dao.UsuarioDAO;
import ec.edu.ups.ppw.integrations.PythonServiceClient;
import ec.edu.ups.ppw.model.Asesoria;
import ec.edu.ups.ppw.model.EstadoAsesoria;
import ec.edu.ups.ppw.model.Programador;
import ec.edu.ups.ppw.model.Usuario;
import ec.edu.ups.ppw.rest.dto.AsesoriaRequest;
import ec.edu.ups.ppw.rest.dto.RechazoRequest;
import ec.edu.ups.ppw.rest.security.UserPrincipal;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.EJB;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;

@Path("/asesorias")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AsesoriaResource {

    @EJB private AsesoriaDAO asesoriaDAO;
    @EJB private UsuarioDAO usuarioDAO;
    @EJB private ProgramadorDAO programadorDAO;
    @EJB private DisponibilidadDAO disponibilidadDAO;

    @Inject private PythonServiceClient python;

    private static final String ADMIN_TELEGRAM_CHAT_ID = "6370661935";

    @POST
    @RolesAllowed({"Usuario", "Admin"})
    public Response solicitar(AsesoriaRequest req, @Context SecurityContext sc) {
        UserPrincipal up = principal(sc);
        if (up == null) return Response.status(Response.Status.UNAUTHORIZED).entity("Unauthorized").build();

        if (req == null || req.programadorId == null || req.fechaInicio == null || req.fechaFin == null || req.modalidad == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Faltan campos").build();
        }

        Usuario cliente = usuarioDAO.findByFirebaseUid(up.getUid());
        if (cliente == null) return Response.status(Response.Status.NOT_FOUND).entity("Usuario no existe en BD").build();

        Programador prog = programadorDAO.readWithUsuario(req.programadorId);
        if (prog == null) return Response.status(Response.Status.NOT_FOUND).entity("Programador no existe").build();

        LocalDateTime inicio;
        LocalDateTime fin;
        try {
            inicio = LocalDateTime.parse(req.fechaInicio);
            fin = LocalDateTime.parse(req.fechaFin);
        } catch (DateTimeParseException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Formato fecha inválido").build();
        }

        if (!inicio.isBefore(fin)) {
            return Response.status(Response.Status.BAD_REQUEST).entity("fechaInicio debe ser menor que fechaFin").build();
        }

        if (asesoriaDAO.existsOverlappingProgramador(req.programadorId, inicio, fin)) {
            return Response.status(Response.Status.CONFLICT).entity("Horario ocupado").build();
        }

        LocalDate f = inicio.toLocalDate();
        LocalTime hi = inicio.toLocalTime();
        LocalTime hf = fin.toLocalTime();

        if (!f.equals(fin.toLocalDate())) {
            return Response.status(Response.Status.BAD_REQUEST).entity("La asesoría debe estar en el mismo día").build();
        }

        boolean okSlot = disponibilidadDAO.existsCoveringSlot(req.programadorId, f, hi, hf, req.modalidad);
        if (!okSlot) {
            return Response.status(Response.Status.CONFLICT).entity("No hay disponibilidad para ese horario/modalidad").build();
        }

        Asesoria a = new Asesoria();
        a.setCliente(cliente);
        a.setProgramador(prog);
        a.setFechaInicio(inicio);
        a.setFechaFin(fin);
        a.setModalidad(req.modalidad);
        a.setEstado(EstadoAsesoria.Pendiente);

        if (req.comentario != null && !req.comentario.isBlank()) {
            a.setComentario(req.comentario.trim());
        }

        asesoriaDAO.insert(a);

        try {
            String toProg = (prog.getUsuario() != null) ? prog.getUsuario().getEmail() : null;

            String subject = "Nueva solicitud de asesoría";
            String text =
                    "Hola,\n\n" +
                    "Tienes una nueva solicitud de asesoría.\n\n" +
                    "Cliente: " + safe(cliente.getNombres()) + " " + safe(cliente.getApellidos()) + "\n" +
                    "Correo cliente: " + safe(cliente.getEmail()) + "\n" +
                    "Inicio: " + inicio + "\n" +
                    "Fin: " + fin + "\n" +
                    "Modalidad: " + req.modalidad + "\n" +
                    (a.getComentario() != null ? ("Comentario: " + a.getComentario() + "\n") : "") +
                    "\nRevisa tu panel para aprobar o rechazar.";

            String replyTo = cliente.getEmail();
            python.sendEmail(toProg, subject, text, replyTo);


            python.sendTelegram(
                ADMIN_TELEGRAM_CHAT_ID,
                "Nueva asesoría creada: " + safe(cliente.getEmail()) +
                " -> ProgID " + req.programadorId +
                " (" + inicio + " a " + fin + ")"
            );
        } catch (Exception e) {
            System.out.println("Notif error (solicitar): " + e.getMessage());
        }

        return Response.status(Response.Status.CREATED).entity(a).build();
    }

    @GET
    @Path("/mias")
    @RolesAllowed({"Usuario", "Admin", "Programador"})
    public Response misAsesorias(@Context SecurityContext sc) {
        UserPrincipal up = principal(sc);
        if (up == null) return Response.status(Response.Status.UNAUTHORIZED).entity("Unauthorized").build();

        Usuario u = usuarioDAO.findByFirebaseUid(up.getUid());
        if (u == null) return Response.status(Response.Status.NOT_FOUND).entity("Usuario no existe en BD").build();

        List<Asesoria> list = asesoriaDAO.listByCliente(u.getId());
        return Response.ok(list).build();
    }

    @GET
    @Path("/recibidas")
    @RolesAllowed({"Programador", "Admin"})
    public Response recibidas(@Context SecurityContext sc) {
        UserPrincipal up = principal(sc);
        if (up == null) return Response.status(Response.Status.UNAUTHORIZED).entity("Unauthorized").build();

        Programador p = programadorDAO.findByUsuarioFirebaseUid(up.getUid());
        if (p == null) return Response.status(Response.Status.NOT_FOUND).entity("No existe Programador asociado").build();

        List<Asesoria> list = asesoriaDAO.listByProgramador(p.getId());
        return Response.ok(list).build();
    }

    @PUT
    @Path("/{id}/aprobar")
    @RolesAllowed({"Programador", "Admin"})
    public Response aprobar(@PathParam("id") Long id, RechazoRequest req, @Context SecurityContext sc) {
        return cambiarEstadoProgramador(id, EstadoAsesoria.Aprobada, req, sc);
    }

    @PUT
    @Path("/{id}/rechazar")
    @RolesAllowed({"Programador", "Admin"})
    public Response rechazar(@PathParam("id") Long id, RechazoRequest req, @Context SecurityContext sc) {
        return cambiarEstadoProgramador(id, EstadoAsesoria.Rechazada, req, sc);
    }

    private Response cambiarEstadoProgramador(Long asesoriaId, EstadoAsesoria nuevoEstado, RechazoRequest req, SecurityContext sc) {
        if (asesoriaId == null) return Response.status(Response.Status.BAD_REQUEST).entity("Id inválido").build();

        UserPrincipal up = principal(sc);
        if (up == null) return Response.status(Response.Status.UNAUTHORIZED).entity("Unauthorized").build();

        Asesoria a = asesoriaDAO.readFull(asesoriaId);
        if (a == null) return Response.status(Response.Status.NOT_FOUND).entity("Asesoría no existe").build();

        if (sc != null && sc.isUserInRole("Programador")) {
            Programador p = programadorDAO.findByUsuarioFirebaseUid(up.getUid());
            if (p == null) return Response.status(Response.Status.NOT_FOUND).entity("No existe Programador asociado").build();

            if (a.getProgramador() == null || a.getProgramador().getId() == null || !a.getProgramador().getId().equals(p.getId())) {
                return Response.status(Response.Status.FORBIDDEN).entity("No autorizado").build();
            }
        }

        a.setEstado(nuevoEstado);

        if (req != null && req.mensaje != null && !req.mensaje.isBlank()) {
            a.setMensajeRespuesta(req.mensaje.trim());
        }

        asesoriaDAO.update(a);

        try {
            String userTo = (a.getCliente() != null) ? a.getCliente().getEmail() : null;

            String subject = "Tu asesoría fue " + (nuevoEstado == EstadoAsesoria.Aprobada ? "APROBADA" : "RECHAZADA");
            String text =
                    "Hola,\n\n" +
                    "Tu solicitud de asesoría ha sido " + (nuevoEstado == EstadoAsesoria.Aprobada ? "APROBADA" : "RECHAZADA") + ".\n\n" +
                    "Inicio: " + a.getFechaInicio() + "\n" +
                    "Fin: " + a.getFechaFin() + "\n" +
                    "Modalidad: " + a.getModalidad() + "\n" +
                    (a.getMensajeRespuesta() != null ? ("Mensaje del programador: " + a.getMensajeRespuesta() + "\n") : "") +
                    "\nGracias.";

            String replyTo = (a.getProgramador() != null && a.getProgramador().getUsuario() != null)
                    ? a.getProgramador().getUsuario().getEmail()
                    : null;

            python.sendEmail(userTo, subject, text, replyTo);

            python.sendTelegram(
                ADMIN_TELEGRAM_CHAT_ID,
                "Asesoría #" + a.getId() + " -> " + nuevoEstado + " | cliente: " + safe(userTo)
            );
        } catch (Exception e) {
            System.out.println("Notif error (cambiarEstado): " + e.getMessage());
        }

        return Response.ok(a).build();
    }

    private UserPrincipal principal(SecurityContext sc) {
        if (sc == null || sc.getUserPrincipal() == null) return null;
        return (sc.getUserPrincipal() instanceof UserPrincipal up) ? up : null;
    }

    private String safe(String s) {
        return (s == null) ? "" : s;
    }
}
