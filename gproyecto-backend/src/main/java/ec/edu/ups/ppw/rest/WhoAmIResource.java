package ec.edu.ups.ppw.rest;

import ec.edu.ups.ppw.dao.ProgramadorDAO;
import ec.edu.ups.ppw.dao.UsuarioDAO;
import ec.edu.ups.ppw.model.Programador;
import ec.edu.ups.ppw.model.Usuario;
import ec.edu.ups.ppw.rest.security.UserPrincipal;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;

@Path("/whoami")
public class WhoAmIResource {

    @Inject
    private UsuarioDAO usuarioDAO;

    @Inject
    private ProgramadorDAO programadorDAO;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response whoami(@Context SecurityContext sc) {
        if (sc == null || sc.getUserPrincipal() == null) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("{\"error\":\"Unauthorized\"}").build();
        }
        if (!(sc.getUserPrincipal() instanceof UserPrincipal up)) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("{\"error\":\"Invalid principal\"}").build();
        }

        Long usuarioId = null;
        Long programadorId = null;

        Usuario u = usuarioDAO.findByFirebaseUid(up.getUid());
        if (u != null) {
            usuarioId = u.getId();
            Programador p = programadorDAO.findByUsuarioId(usuarioId);
            if (p != null) programadorId = p.getId();
        }

        String json = "{"
                + "\"uid\":\"" + safe(up.getUid()) + "\","
                + "\"email\":\"" + safe(up.getEmail()) + "\","
                + "\"name\":\"" + safe(up.getDisplayName()) + "\","
                + "\"rol\":\"" + safe(up.getRol()) + "\","
                + "\"usuarioId\":" + (usuarioId == null ? "null" : usuarioId) + ","
                + "\"programadorId\":" + (programadorId == null ? "null" : programadorId)
                + "}";

        return Response.ok(json).build();
    }

    @GET
    @Path("/usuario")
    @RolesAllowed({"Usuario"})
    @Produces(MediaType.TEXT_PLAIN)
    public String soloUsuario() {
        return "OK Usuario";
    }

    @GET
    @Path("/programador")
    @RolesAllowed({"Programador"})
    @Produces(MediaType.TEXT_PLAIN)
    public String soloProgramador() {
        return "OK Programador";
    }

    @GET
    @Path("/admin")
    @RolesAllowed({"Admin"})
    @Produces(MediaType.TEXT_PLAIN)
    public String soloAdmin() {
        return "OK Admin";
    }

    private String safe(String s) {
        if (s == null) return "";
        return s.replace("\"", "\\\"");
    }
}
