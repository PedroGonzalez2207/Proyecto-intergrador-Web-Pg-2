package ec.edu.ups.ppw.rest.filters;

import java.io.StringReader;
import java.security.Principal;

import ec.edu.ups.ppw.rest.security.UserPrincipal;
import jakarta.annotation.Priority;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.ext.Provider;

@Provider
@Priority(Priorities.AUTHENTICATION)
public class FirebaseAuthFilter implements ContainerRequestFilter {

    // OJO: esto debe existir en Spring Boot
    private static final String AUTH_VERIFY_URL = "http://localhost:8081/api/auth/verify";
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
                    .target(AUTH_VERIFY_URL)
                    .request(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token);

            resp = req.get();

            if (resp.getStatus() != 200) {
                abort(requestContext, 401, "Invalid or expired token");
                return;
            }

            String body = resp.readEntity(String.class);
            JsonObject json = Json.createReader(new StringReader(body)).readObject();

            // Esperamos uid/email/name
            String uid = json.getString("uid", "");
            String email = json.getString("email", "");
            String name = json.getString("name", "");

            // Rol vacío aquí, luego se puede completar con RoleAuthorizationFilter
            final UserPrincipal principal = new UserPrincipal(uid, email, name, "");

            final boolean isSecure = "https".equalsIgnoreCase(
                    requestContext.getUriInfo().getRequestUri().getScheme()
            );

            requestContext.setSecurityContext(new SecurityContext() {
                @Override public Principal getUserPrincipal() { return principal; }
                @Override public boolean isUserInRole(String role) { return false; }
                @Override public boolean isSecure() { return isSecure; }
                @Override public String getAuthenticationScheme() { return "Bearer"; }
            });

        } catch (Exception e) {
            abort(requestContext, 401, "Invalid or expired token");
        } finally {
            if (resp != null) resp.close();
        }
    }

    private void abort(ContainerRequestContext ctx, int status, String msg) {
        ctx.abortWith(Response.status(status)
                .type(MediaType.TEXT_PLAIN)
                .entity(msg)
                .build());
    }
}
