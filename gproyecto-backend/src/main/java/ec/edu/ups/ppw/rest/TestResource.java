package ec.edu.ups.ppw.rest;

import ec.edu.ups.ppw.rest.security.UserPrincipal;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;

@Path("/test")
public class TestResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response ping(@Context SecurityContext securityContext) {

        if (securityContext == null || securityContext.getUserPrincipal() == null) {
            return Response.status(401)
                    .entity("{\"ok\":false,\"error\":\"No principal\"}")
                    .build();
        }

        String uid = securityContext.getUserPrincipal().getName();
        String email = "";
        String name = "";

        if (securityContext.getUserPrincipal() instanceof UserPrincipal up) {
            email = up.getEmail();
            name = up.getDisplayName();
        }

        String json = "{\"ok\":true,\"uid\":\"" + safe(uid) + "\",\"email\":\"" + safe(email) + "\",\"name\":\"" + safe(name) + "\"}";
        return Response.ok(json).build();
    }

    private String safe(String s) {
        if (s == null) return "";
        return s.replace("\"", "\\\"");
    }
}
