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

    // ✅ Emails admin (siempre en minúsculas)
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
        String email = safe(up.getEmail()).toLowerCase();        // normalizado
        String displayName = safe(up.getDisplayName());

        if (uid.isBlank()) {
            requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).entity("Unauthorized").build());
            return;
        }

        try {
            // 1) Buscar por firebaseUid
            Usuario u = usuarioDAO.findByFirebaseUid(uid);

            // 2) Si no existe, intenta por email y vincula uid
            if (u == null && !email.isBlank()) {
                u = usuarioDAO.findByEmail(email);
                if (u != null && isBlank(u.getFirebaseUid())) {
                    u.setFirebaseUid(uid);
                    u = usuarioDAO.update(u);
                }
            }

            // 3) Si no existe -> crear
            if (u == null) {
                u = new Usuario();
                u.setFirebaseUid(uid);
                u.setEmail(email.isBlank() ? (uid + "@firebase.local") : email);
                u.setNombres(displayName.isBlank() ? "Usuario" : displayName);
                u.setApellidos("");
                u.setActivo(true);

                // ✅ rol default: si email está en allowlist -> ADMIN, sino USUARIO
                Rol rolDefault = (!email.isBlank() && ADMIN_EMAILS.contains(email)) ? Rol.Admin : Rol.Usuario;
                u.setRol(rolDefault);

                usuarioDAO.insert(u);

            } else {
                // 4) Mantener datos actualizados (si llegan desde Firebase)
                boolean changed = false;

                if (!email.isBlank() && (u.getEmail() == null || !u.getEmail().equalsIgnoreCase(email))) {
                    u.setEmail(email);
                    changed = true;
                }

                if (!displayName.isBlank() && (u.getNombres() == null || !u.getNombres().equals(displayName))) {
                    u.setNombres(displayName);
                    changed = true;
                }

                // 5) Si ya existe y su email está en allowlist => forzar ADMIN
                if (!email.isBlank() && ADMIN_EMAILS.contains(email) && u.getRol() != Rol.Admin) {
                    u.setRol(Rol.Admin);
                    changed = true;
                }

                // 6) Rol default si viene nulo
                if (u.getRol() == null) {
                    u.setRol(Rol.Usuario);
                    changed = true;
                }

                if (changed) {
                    u = usuarioDAO.update(u);
                }
            }

            final String rolDb = (u.getRol() == null) ? Rol.Usuario.name() : u.getRol().name();
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

                    // ✅ Admin entra a todo
                    if ("Admin".equalsIgnoreCase(enriched.getRol())) return true;

                    // match directo
                    return role.equalsIgnoreCase(enriched.getRol());
                }

                @Override public boolean isSecure() { return isSecure; }
                @Override public String getAuthenticationScheme() { return "Bearer"; }
            });

        } catch (Exception e) {
            requestContext.abortWith(
                Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Auth error")
                    .build()
            );
        }
    }

    private static String safe(String s) {
        return s == null ? "" : s.trim();
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
