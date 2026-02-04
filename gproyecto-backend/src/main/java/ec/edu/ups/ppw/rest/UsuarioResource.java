package ec.edu.ups.ppw.rest;

import java.util.List;

import ec.edu.ups.ppw.dao.ProgramadorDAO;
import ec.edu.ups.ppw.dao.UsuarioDAO;
import ec.edu.ups.ppw.model.Programador;
import ec.edu.ups.ppw.model.Rol;
import ec.edu.ups.ppw.model.Usuario;
import ec.edu.ups.ppw.rest.dto.RegisterRequest;
import jakarta.ejb.EJB;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/usuarios")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class UsuarioResource {

    @EJB
    private UsuarioDAO usuarioDAO;

    @EJB
    private ProgramadorDAO programadorDAO;

    @POST
    public Response register(RegisterRequest req) {
        if (req == null || req.email == null || req.email.isBlank() || req.password == null || req.password.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST).entity("email y password son requeridos").build();
        }
        if (req.rol == null) req.rol = Rol.Usuario;

        if (usuarioDAO.findByEmail(req.email) != null) {
            return Response.status(Response.Status.CONFLICT).entity("Ya existe un usuario con ese email").build();
        }

        Usuario u = new Usuario();
        u.setEmail(req.email);
        u.setPasswordHash(req.password); 
        u.setNombres(req.nombres == null ? "" : req.nombres);
        u.setApellidos(req.apellidos == null ? "" : req.apellidos);
        u.setTelefono(req.telefono);
        u.setRol(req.rol);

        usuarioDAO.insert(u);

        // Perfiles
        if (req.rol == Rol.Programador) {
            Programador p = new Programador();
            p.setUsuario(u);
            p.setBio("");
            p.setEspecialidades("");
            programadorDAO.insert(p);
        }

        return Response.status(Response.Status.CREATED).entity(u).build();
    }

    @GET
    public List<Usuario> listar() {
    	return usuarioDAO.getAll();
    }
}
