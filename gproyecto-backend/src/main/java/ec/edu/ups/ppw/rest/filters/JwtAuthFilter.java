package ec.edu.ups.ppw.rest.filters;

import java.nio.charset.StandardCharsets;
import java.security.Principal;

import ec.edu.ups.ppw.rest.security.UserPrincipal;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;

import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.ext.Provider;

@Provider
@Priority(Priorities.AUTHENTICATION)
public class JwtAuthFilter implements ContainerRequestFilter {

    private static final String JWT_SECRET = "CLAVE_SUPER_SEGURA_32_PEDRO_GONZALEZ";

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

        try {
            Jws<Claims> jws = Jwts.parserBuilder()
                    .setSigningKey(JWT_SECRET.getBytes(StandardCharsets.UTF_8))
                    .build()
                    .parseClaimsJws(token);

            Claims c = jws.getBody();

            String subject = c.getSubject() != null ? c.getSubject() : "";
            String uid = str(c.get("uid"));
            String email = str(c.get("email"));
            String rol = str(c.get("rol"));

            if (uid.isBlank()) uid = subject;
            if (email.isBlank()) email = subject;

            final UserPrincipal principal = new UserPrincipal(uid, email, "", rol);
            final boolean isSecure = requestContext.getUriInfo().getRequestUri().getScheme().equalsIgnoreCase("https");

            requestContext.setSecurityContext(new SecurityContext() {
                @Override public Principal getUserPrincipal() { return principal; }
                @Override public boolean isUserInRole(String role) {
                    if (role == null) return false;
                    return role.equalsIgnoreCase(principal.getRol());
                }
                @Override public boolean isSecure() { return isSecure; }
                @Override public String getAuthenticationScheme() { return "Bearer"; }
            });

        } catch (Exception e) {
            abort(requestContext, 401, "Invalid or expired token");
        }
    }

    private String str(Object v) { return v == null ? "" : v.toString(); }

    private void abort(ContainerRequestContext ctx, int status, String msg) {
        ctx.abortWith(Response.status(status).type(MediaType.TEXT_PLAIN).entity(msg).build());
    }
}
