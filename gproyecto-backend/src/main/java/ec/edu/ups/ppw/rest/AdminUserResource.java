package ec.edu.ups.ppw.rest;

import java.util.List;

import ec.edu.ups.ppw.dao.ProgramadorDAO;
import ec.edu.ups.ppw.dao.UsuarioDAO;
import ec.edu.ups.ppw.model.Programador;
import ec.edu.ups.ppw.model.Rol;
import ec.edu.ups.ppw.model.Usuario;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;

@Path("/admin/users")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AdminUserResource {

    @Inject private UsuarioDAO usuarioDAO;
    @Inject private ProgramadorDAO programadorDAO;

    private boolean isAdmin(SecurityContext sc) {
        return sc != null && sc.isUserInRole("Admin");
    }

    private Rol parseRol(String rol) {
        if (rol == null || rol.isBlank()) throw new BadRequestException("Invalid rol");
        for (Rol r : Rol.values()) {
            if (r.name().equalsIgnoreCase(rol.trim())) return r;
        }
        throw new BadRequestException("Invalid rol");
    }

    @GET
    public Response list(@Context SecurityContext sc) {
        if (!isAdmin(sc)) return Response.status(Response.Status.FORBIDDEN).entity("{\"error\":\"Forbidden\"}").build();
        List<Usuario> users = usuarioDAO.getAll();
        return Response.ok(users).build();
    }

    @PUT
    @Path("/by-id/{id}/rol/{rol}")
    public Response setRolById(@Context SecurityContext sc,
                               @PathParam("id") Long id,
                               @PathParam("rol") String rol) {

        if (!isAdmin(sc)) return Response.status(Response.Status.FORBIDDEN).entity("{\"error\":\"Forbidden\"}").build();

        Usuario u = usuarioDAO.read(id);
        if (u == null) return Response.status(Response.Status.NOT_FOUND).entity("{\"error\":\"User not found\"}").build();

        Rol nuevo;
        try { nuevo = parseRol(rol); }
        catch (BadRequestException e) { return Response.status(Response.Status.BAD_REQUEST).entity("{\"error\":\"Invalid rol\"}").build(); }

        Rol anterior = u.getRol();
        u.setRol(nuevo);
        usuarioDAO.update(u);

        if (anterior != Rol.Programador && nuevo == Rol.Programador) {
            Programador p = programadorDAO.findByUsuarioId(u.getId());
            if (p == null) {
                Programador np = new Programador();
                np.setUsuario(u);
                np.setBio("");
                np.setEspecialidades("");
                programadorDAO.insert(np);
            }
        }

        return Response.ok("{\"ok\":true,\"id\":" + id + ",\"rol\":\"" + u.getRol().name() + "\"}").build();
    }

    @PUT
    @Path("/by-email/{email}/rol/{rol}")
    public Response setRolByEmail(@Context SecurityContext sc,
                                  @PathParam("email") String email,
                                  @PathParam("rol") String rol) {

        if (!isAdmin(sc)) return Response.status(Response.Status.FORBIDDEN).entity("{\"error\":\"Forbidden\"}").build();

        String emailNorm = email == null ? "" : email.trim().toLowerCase();
        Usuario u = usuarioDAO.findByEmail(emailNorm);
        if (u == null) return Response.status(Response.Status.NOT_FOUND).entity("{\"error\":\"User not found\"}").build();

        Rol nuevo;
        try { nuevo = parseRol(rol); }
        catch (BadRequestException e) { return Response.status(Response.Status.BAD_REQUEST).entity("{\"error\":\"Invalid rol\"}").build(); }

        Rol anterior = u.getRol();
        u.setRol(nuevo);
        usuarioDAO.update(u);

        if (anterior != Rol.Programador && nuevo == Rol.Programador) {
            Programador p = programadorDAO.findByUsuarioId(u.getId());
            if (p == null) {
                Programador np = new Programador();
                np.setUsuario(u);
                np.setBio("");
                np.setEspecialidades("");
                programadorDAO.insert(np);
            }
        }

        return Response.ok("{\"ok\":true,\"email\":\"" + safe(emailNorm) + "\",\"rol\":\"" + u.getRol().name() + "\"}").build();
    }

    private String safe(String s) {
        if (s == null) return "";
        return s.replace("\"", "\\\"");
    }
}
