package ec.edu.ups.ppw.rest.filters;

import java.security.Principal;
import java.util.Set;

import ec.edu.ups.ppw.dao.UsuarioDAO;
import ec.edu.ups.ppw.model.Rol;
import ec.edu.ups.ppw.model.Usuario;
import ec.edu.ups.ppw.rest.security.UserPrincipal;
import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.ext.Provider;

@Provider
@Priority(Priorities.AUTHORIZATION)
public class RoleAuthorizationFilter implements ContainerRequestFilter {

    private static final Set<String> ADMIN_EMAILS = Set.of(
        "pedrojose.g2207@gmail.com"
    );

    @Inject
    private UsuarioDAO usuarioDAO;

    @Override
    public void filter(ContainerRequestContext requestContext) {

        SecurityContext sc = requestContext.getSecurityContext();
        if (sc == null || sc.getUserPrincipal() == null) {
            requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).entity("Unauthorized").build());
            return;
        }

        Principal p = sc.getUserPrincipal();
        if (!(p instanceof UserPrincipal up)) {
            requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).entity("Unauthorized").build());
            return;
        }

        String uid = safe(up.getUid());
        String email = safe(up.getEmail()).toLowerCase();
        String displayName = safe(up.getDisplayName());

        if (uid.isBlank()) {
            requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).entity("Unauthorized").build());
            return;
        }

        Usuario u = usuarioDAO.findByFirebaseUid(uid);

        if (u == null && !email.isBlank()) {
            u = usuarioDAO.findByEmail(email);
            if (u != null && isBlank(u.getFirebaseUid())) {
                u.setFirebaseUid(uid);
                u = usuarioDAO.update(u);
            }
        }

        if (u == null) {
            u = new Usuario();
            u.setFirebaseUid(uid);
            u.setEmail(email.isBlank() ? (uid + "@firebase.local") : email);
            u.setNombres(displayName.isBlank() ? (email.isBlank() ? "Usuario" : email) : displayName);
            u.setApellidos("");
            u.setActivo(true);
            u.setRol((!email.isBlank() && ADMIN_EMAILS.contains(email)) ? Rol.Admin : Rol.Usuario);
            usuarioDAO.insert(u);
        } else {
            boolean changed = false;

            if (!email.isBlank() && (u.getEmail() == null || !u.getEmail().equalsIgnoreCase(email))) {
                u.setEmail(email);
                changed = true;
            }

            if (!displayName.isBlank()) {
                String currentName = safe(u.getNombres());
                if (currentName.isBlank() || currentName.equalsIgnoreCase("usuario")) {
                    u.setNombres(displayName);
                    changed = true;
                }
            }

            if (!email.isBlank() && ADMIN_EMAILS.contains(email) && u.getRol() != Rol.Admin) {
                u.setRol(Rol.Admin);
                changed = true;
            }

            if (u.getRol() == null) {
                u.setRol(Rol.Usuario);
                changed = true;
            }

            if (changed) {
                u = usuarioDAO.update(u);
            }
        }

        final String rolDb = u.getRol() == null ? Rol.Usuario.name() : u.getRol().name();
        final boolean isSecure = sc.isSecure();

        final UserPrincipal enriched = new UserPrincipal(
                uid,
                u.getEmail(),
                u.getNombres(),
                rolDb
        );

        requestContext.setSecurityContext(new SecurityContext() {
            @Override public Principal getUserPrincipal() { return enriched; }

            @Override public boolean isUserInRole(String role) {
                if (role == null) return false;

                if ("Admin".equalsIgnoreCase(enriched.getRol())) return true;

                if ("ADMIN".equalsIgnoreCase(role)) role = "Admin";
                if ("PROGRAMADOR".equalsIgnoreCase(role)) role = "Programador";
                if ("USUARIO".equalsIgnoreCase(role) || "CLIENTE".equalsIgnoreCase(role)) role = "Usuario";

                return role.equalsIgnoreCase(enriched.getRol());
            }

            @Override public boolean isSecure() { return isSecure; }
            @Override public String getAuthenticationScheme() { return "Bearer"; }
        });
    }

    private static String safe(String s) {
        return s == null ? "" : s.trim();
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
