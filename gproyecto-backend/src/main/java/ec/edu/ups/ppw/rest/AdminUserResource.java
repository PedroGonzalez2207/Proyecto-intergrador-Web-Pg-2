package ec.edu.ups.ppw.rest;

import java.util.List;

import ec.edu.ups.ppw.dao.UsuarioDAO;
import ec.edu.ups.ppw.model.Rol;
import ec.edu.ups.ppw.model.Usuario;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;

@Path("/admin/users")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AdminUserResource {

    @Inject
    private UsuarioDAO usuarioDAO;

    private boolean isAdmin(SecurityContext sc) {
        return sc != null && sc.isUserInRole("ADMIN");
    }

    // GET /api/admin/users
    @GET
    public Response list(@Context SecurityContext sc) {
        if (!isAdmin(sc)) return Response.status(403).entity("{\"error\":\"Forbidden\"}").build();

        List<Usuario> users = usuarioDAO.getAll();
        return Response.ok(users).build();
    }

    // PUT /api/admin/users/by-id/3/rol/PROGRAMADOR
    @PUT
    @Path("/by-id/{id}/rol/{rol}")
    public Response setRolById(@Context SecurityContext sc,
                               @PathParam("id") Long id,
                               @PathParam("rol") String rol) {
        if (!isAdmin(sc)) return Response.status(403).entity("{\"error\":\"Forbidden\"}").build();

        Usuario u = usuarioDAO.read(id);
        if (u == null) return Response.status(404).entity("{\"error\":\"User not found\"}").build();

        Rol nuevo;
        try { nuevo = Rol.valueOf(rol.toUpperCase()); }
        catch (Exception e) { return Response.status(400).entity("{\"error\":\"Invalid rol\"}").build(); }

        u.setRol(nuevo);
        usuarioDAO.update(u);

        return Response.ok("{\"ok\":true,\"id\":" + id + ",\"rol\":\"" + u.getRol().name() + "\"}").build();
    }

    // PUT /api/admin/users/by-email/alguien@gmail.com/rol/PROGRAMADOR
    @PUT
    @Path("/by-email/{email}/rol/{rol}")
    public Response setRolByEmail(@Context SecurityContext sc,
                                  @PathParam("email") String email,
                                  @PathParam("rol") String rol) {
        if (!isAdmin(sc)) return Response.status(403).entity("{\"error\":\"Forbidden\"}").build();

        Usuario u = usuarioDAO.findByEmail(email);
        if (u == null) return Response.status(404).entity("{\"error\":\"User not found\"}").build();

        Rol nuevo;
        try { nuevo = Rol.valueOf(rol.toUpperCase()); }
        catch (Exception e) { return Response.status(400).entity("{\"error\":\"Invalid rol\"}").build(); }

        u.setRol(nuevo);
        usuarioDAO.update(u);

        return Response.ok("{\"ok\":true,\"email\":\"" + safe(email) + "\",\"rol\":\"" + u.getRol().name() + "\"}").build();
    }

    private String safe(String s) {
        if (s == null) return "";
        return s.replace("\"", "\\\"");
    }
}
