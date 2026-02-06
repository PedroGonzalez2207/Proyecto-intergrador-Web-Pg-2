package ec.edu.ups.ppw.rest.filters;

import java.io.StringReader;
import java.security.Principal;
import java.util.HashSet;
import java.util.Set;

import ec.edu.ups.ppw.rest.security.UserPrincipal;
import jakarta.annotation.Priority;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.ext.Provider;

@Provider
@Priority(Priorities.AUTHENTICATION)
public class FirebaseAuthFilter implements ContainerRequestFilter {

    private static final String AUTH_INTROSPECT_URL = "http://127.0.0.1:8081/api/auth/introspect";
    private static final Client CLIENT = ClientBuilder.newClient();

    @Override
    public void filter(ContainerRequestContext requestContext) {

        if ("OPTIONS".equalsIgnoreCase(requestContext.getMethod())) return;

        String authHeader = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            abort(requestContext, 401, "Missing Bearer token");
            return;
        }

        String token = authHeader.substring("Bearer ".length()).trim();
        if (token.isEmpty()) {
            abort(requestContext, 401, "Empty token");
            return;
        }

        Response resp = null;
        try {
            Invocation.Builder req = CLIENT
                    .target(AUTH_INTROSPECT_URL)
                    .request(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token);

            resp = req.get();

            if (resp.getStatus() != 200) {
                abort(requestContext, 401, "Invalid or expired token (introspect not 200)");
                return;
            }

            String body = resp.readEntity(String.class);
            JsonObject claims = Json.createReader(new StringReader(body)).readObject();

            String email = claims.getString("email", "");
            String sub = claims.getString("sub", "");
            String name = claims.getString("name", "");
            String rolRaw = claims.getString("rol", "");

            String uid = !sub.isBlank() ? sub : email;
            String rol = normalizarRol(rolRaw);

            Set<String> roles = new HashSet<>();
            if (!rol.isBlank()) roles.add(rol);

            final UserPrincipal principal = new UserPrincipal(uid, email, name, rol);

            final SecurityContext original = requestContext.getSecurityContext();
            final boolean isSecure = original != null ? original.isSecure()
                    : "https".equalsIgnoreCase(requestContext.getUriInfo().getRequestUri().getScheme());

            requestContext.setSecurityContext(new SecurityContext() {
                @Override
                public Principal getUserPrincipal() {
                    return principal;
                }

                @Override
                public boolean isUserInRole(String role) {
                    return role != null && roles.contains(role);
                }

                @Override
                public boolean isSecure() {
                    return isSecure;
                }

                @Override
                public String getAuthenticationScheme() {
                    return "Bearer";
                }
            });

        } catch (Exception e) {
            abort(requestContext, 401, "Invalid or expired token");
        } finally {
            if (resp != null) resp.close();
        }
    }

    private String normalizarRol(String rolRaw) {
        if (rolRaw == null) return "";
        String r = rolRaw.trim();
        if (r.equalsIgnoreCase("CLIENTE") || r.equalsIgnoreCase("USER") || r.equalsIgnoreCase("USUARIO")) return "Usuario";
        if (r.equalsIgnoreCase("PROGRAMADOR") || r.equalsIgnoreCase("PROGRAMMER")) return "Programador";
        if (r.equalsIgnoreCase("ADMIN") || r.equalsIgnoreCase("ADMINISTRADOR")) return "Admin";
        if (r.equalsIgnoreCase("Usuario") || r.equalsIgnoreCase("Programador") || r.equalsIgnoreCase("Admin")) {
            return r.substring(0, 1).toUpperCase() + r.substring(1).toLowerCase();
        }
        return r;
    }

    private void abort(ContainerRequestContext ctx, int status, String msg) {
        ctx.abortWith(Response.status(status)
                .type(MediaType.TEXT_PLAIN)
                .entity(msg)
                .build());
    }
}
