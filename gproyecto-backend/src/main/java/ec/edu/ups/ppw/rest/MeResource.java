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
        String email = up.getEmail();
        String fullName = up.getDisplayName();

        // 1) Buscar por firebaseUid
        Usuario u = usuarioDAO.findByFirebaseUid(uid);

        // 2) Si no existe por uid, buscar por email (para emparejar seed/admin)
        if (u == null && email != null && !email.isBlank()) {
            u = usuarioDAO.findByEmail(email);
            if (u != null) {
                // Emparejar firebaseUid y actualizar datos básicos
                u.setFirebaseUid(uid);

                if (fullName != null && !fullName.isBlank()) {
                    String[] parts = fullName.trim().split("\\s+");
                    if (parts.length == 1) {
                        u.setNombres(parts[0]);
                        if (u.getApellidos() == null) u.setApellidos("");
                    } else {
                        u.setNombres(parts[0]);
                        u.setApellidos(String.join(" ", java.util.Arrays.copyOfRange(parts, 1, parts.length)));
                    }
                }
                u = usuarioDAO.update(u);
            }
        }

        // 3) Si aún no existe, crear como CLIENTE
        if (u == null) {
            u = new Usuario();
            u.setFirebaseUid(uid);
            u.setEmail(email);

            String[] parts = (fullName == null ? "" : fullName.trim()).split("\\s+");
            if (parts.length == 0 || parts[0].isBlank()) {
                u.setNombres("Usuario");
                u.setApellidos("");
            } else if (parts.length == 1) {
                u.setNombres(parts[0]);
                u.setApellidos("");
            } else {
                u.setNombres(parts[0]);
                u.setApellidos(String.join(" ", java.util.Arrays.copyOfRange(parts, 1, parts.length)));
            }

            u.setRol(Rol.Usuario);
            usuarioDAO.create(u);
        } else {
            // 4) Si existe, actualizar email/nombre si cambiaron (sin tocar rol)
            boolean changed = false;

            if (email != null && !email.isBlank() && !email.equals(u.getEmail())) {
                u.setEmail(email);
                changed = true;
            }

            if ((u.getNombres() == null || u.getNombres().isBlank()) && fullName != null && !fullName.isBlank()) {
                String[] parts = fullName.trim().split("\\s+");
                u.setNombres(parts[0]);
                if (parts.length > 1) {
                    u.setApellidos(String.join(" ", java.util.Arrays.copyOfRange(parts, 1, parts.length)));
                }
                changed = true;
            }

            if (changed) {
                u = usuarioDAO.update(u);
            }
        }

        // 5) Respuesta
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

    private String safe(String s) {
        if (s == null) return "";
        return s.replace("\"", "\\\"");
    }
}
