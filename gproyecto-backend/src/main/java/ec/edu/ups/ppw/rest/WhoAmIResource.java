package ec.edu.ups.ppw.rest;

import ec.edu.ups.ppw.rest.security.UserPrincipal;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;

@Path("/whoami")
public class WhoAmIResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response whoami(@Context SecurityContext sc) {
        if (sc == null || sc.getUserPrincipal() == null) {
            return Response.status(401).entity("{\"error\":\"Unauthorized\"}").build();
        }

        if (!(sc.getUserPrincipal() instanceof UserPrincipal up)) {
            return Response.status(401).entity("{\"error\":\"Invalid principal\"}").build();
        }

        String json = "{"
                + "\"uid\":\"" + safe(up.getUid()) + "\","
                + "\"email\":\"" + safe(up.getEmail()) + "\","
                + "\"name\":\"" + safe(up.getDisplayName()) + "\","
                + "\"rol\":\"" + safe(up.getRol()) + "\""
                + "}";

        return Response.ok(json).build();
    }

    // Solo para probar seguridad por rol (CLIENTE)
    @GET
    @Path("/cliente")
    @RolesAllowed({"CLIENTE"})
    @Produces(MediaType.TEXT_PLAIN)
    public String soloCliente() {
        return "OK CLIENTE";
    }

    @GET
    @Path("/programador")
    @RolesAllowed({"PROGRAMADOR"})
    @Produces(MediaType.TEXT_PLAIN)
    public String soloProgramador() {
        return "OK PROGRAMADOR";
    }

    @GET
    @Path("/admin")
    @RolesAllowed({"ADMIN"})
    @Produces(MediaType.TEXT_PLAIN)
    public String soloAdmin() {
        return "OK ADMIN";
    }

    private String safe(String s) {
        if (s == null) return "";
        return s.replace("\"", "\\\"");
    }
}
