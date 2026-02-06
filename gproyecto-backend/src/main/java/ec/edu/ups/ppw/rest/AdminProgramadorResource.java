package ec.edu.ups.ppw.rest;

import ec.edu.ups.ppw.dao.ProgramadorDAO;
import ec.edu.ups.ppw.dao.UsuarioDAO;
import ec.edu.ups.ppw.model.Programador;
import ec.edu.ups.ppw.model.Rol;
import ec.edu.ups.ppw.model.Usuario;
import ec.edu.ups.ppw.rest.dto.AdminCrearProgramadorRequest;
import jakarta.ejb.EJB;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;

@Path("/admin/programadores")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AdminProgramadorResource {

    @EJB private UsuarioDAO usuarioDAO;
    @EJB private ProgramadorDAO programadorDAO;

    private boolean isAdmin(SecurityContext sc) {
        return sc != null && sc.isUserInRole("Admin");
    }

    @POST
    public Response crear(@Context SecurityContext sc, AdminCrearProgramadorRequest req) {
        if (!isAdmin(sc)) return Response.status(Response.Status.FORBIDDEN).entity("Forbidden").build();
        if (req == null || req.email == null || req.email.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Email requerido").build();
        }

        String email = req.email.trim().toLowerCase();
        String nombre = req.nombre == null ? "" : req.nombre.trim();

        Usuario u = usuarioDAO.findByEmail(email);

        if (u == null) {
            u = new Usuario();
            u.setEmail(email);
            u.setNombres(nombre.isBlank() ? "Usuario" : nombre);
            u.setApellidos("");
            u.setActivo(true);
            u.setRol(Rol.Programador);
            usuarioDAO.insert(u);
        } else {
            if (u.getRol() != Rol.Programador) {
                u.setRol(Rol.Programador);
                u = usuarioDAO.update(u);
            }
        }

        Programador p = programadorDAO.findByUsuarioId(u.getId());
        if (p == null) {
            p = new Programador();
            p.setUsuario(u);
            p.setBio(req.bio == null ? "" : req.bio.trim());
            p.setEspecialidades(req.especialidades == null ? "" : req.especialidades.trim());
            programadorDAO.insert(p);
            return Response.status(Response.Status.CREATED).entity(p).build();
        }

        if (req.bio != null) p.setBio(req.bio.trim());
        if (req.especialidades != null) p.setEspecialidades(req.especialidades.trim());
        p = programadorDAO.update(p);

        return Response.ok(p).build();
    }
}
