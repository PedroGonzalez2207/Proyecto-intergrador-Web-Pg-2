package ec.edu.ups.ppw.rest;

import ec.edu.ups.ppw.dao.UsuarioDAO;
import ec.edu.ups.ppw.model.Rol;
import ec.edu.ups.ppw.model.Usuario;
import ec.edu.ups.ppw.rest.security.UserPrincipal;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;

@Path("/me")
public class MeResource {

    @Inject
    private UsuarioDAO usuarioDAO;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response me(@Context SecurityContext securityContext) {

        if (securityContext == null || securityContext.getUserPrincipal() == null) {
            return Response.status(401).entity("{\"error\":\"Unauthorized\"}").build();
        }

        if (!(securityContext.getUserPrincipal() instanceof UserPrincipal up)) {
            return Response.status(401).entity("{\"error\":\"Invalid principal\"}").build();
        }

        String uid = up.getUid();
        String email = up.getEmail() == null ? "" : up.getEmail().trim().toLowerCase();
        String fullName = up.getDisplayName() == null ? "" : up.getDisplayName().trim();

        Usuario u = usuarioDAO.findByFirebaseUid(uid);

        if (u == null && !email.isBlank()) {
            u = usuarioDAO.findByEmail(email);
            if (u != null) {
                u.setFirebaseUid(uid);
                aplicarNombre(u, fullName);
                u = usuarioDAO.update(u);
            }
        }

        if (u == null) {
            u = new Usuario();
            u.setFirebaseUid(uid);
            u.setEmail(email.isBlank() ? (uid + "@firebase.local") : email);
            aplicarNombre(u, fullName);
            u.setRol(Rol.Usuario);
            u.setActivo(true);
            usuarioDAO.insert(u);
        } else {
            boolean changed = false;

            if (!email.isBlank() && (u.getEmail() == null || !email.equalsIgnoreCase(u.getEmail().trim()))) {
                u.setEmail(email);
                changed = true;
            }

            if ((u.getNombres() == null || u.getNombres().isBlank()) && !fullName.isBlank()) {
                aplicarNombre(u, fullName);
                changed = true;
            }

            if (changed) {
                u = usuarioDAO.update(u);
            }
        }

        String json =
                "{"
                + "\"id\":" + u.getId() + ","
                + "\"firebaseUid\":\"" + safe(u.getFirebaseUid()) + "\","
                + "\"email\":\"" + safe(u.getEmail()) + "\","
                + "\"nombres\":\"" + safe(u.getNombres()) + "\","
                + "\"apellidos\":\"" + safe(u.getApellidos()) + "\","
                + "\"telefono\":\"" + safe(u.getTelefono()) + "\","
                + "\"rol\":\"" + (u.getRol() == null ? "" : u.getRol().name()) + "\","
                + "\"activo\":" + (u.getActivo() != null && u.getActivo()) + ","
                + "\"createdAt\":\"" + (u.getCreatedAt() == null ? "" : u.getCreatedAt().toString()) + "\""
                + "}";

        return Response.ok(json).build();
    }

    private void aplicarNombre(Usuario u, String fullName) {
        if (fullName == null || fullName.isBlank()) {
            if (u.getNombres() == null || u.getNombres().isBlank()) u.setNombres("Usuario");
            if (u.getApellidos() == null) u.setApellidos("");
            return;
        }

        String[] parts = fullName.trim().split("\\s+");
        if (parts.length == 1) {
            u.setNombres(parts[0]);
            if (u.getApellidos() == null) u.setApellidos("");
        } else {
            u.setNombres(parts[0]);
            StringBuilder ap = new StringBuilder();
            for (int i = 1; i < parts.length; i++) {
                if (i > 1) ap.append(" ");
                ap.append(parts[i]);
            }
            u.setApellidos(ap.toString());
        }
    }

    private String safe(String s) {
        if (s == null) return "";
        return s.replace("\"", "\\\"");
    }
}
