package ec.edu.ups.ppw.rest.filters;

import java.io.StringReader;

import jakarta.annotation.Priority;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.ext.Provider;

@Provider
@Priority(Priorities.AUTHENTICATION)
public class FirebaseAuthFilter implements ContainerRequestFilter {

    // URL del que valida el token
    private static final String AUTH_VERIFY_URL = "http://localhost:8081/api/auth/verify";

    @Override
    public void filter(ContainerRequestContext requestContext) {

        if ("OPTIONS".equalsIgnoreCase(requestContext.getMethod())) {
            return;
        }

        // 2) Leer token
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

        // 3) Validar token 
        Client client = ClientBuilder.newClient();
        Response resp = null;

        try {
            Invocation.Builder req = client
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

            requestContext.setProperty("uid", json.getString("uid", ""));

            JsonValue claimsVal = json.get("claims");
            if (claimsVal != null && claimsVal.getValueType() == JsonValue.ValueType.OBJECT) {
                JsonObject claims = json.getJsonObject("claims");
                requestContext.setProperty("email", claims.getString("email", ""));
            }

        } catch (Exception e) {
            abort(requestContext, 401, "Invalid or expired token");
        } finally {
            if (resp != null) resp.close();
            client.close();
        }
    }

    private void abort(ContainerRequestContext ctx, int status, String msg) {
        ctx.abortWith(Response.status(status)
                .type(MediaType.TEXT_PLAIN)
                .entity(msg)
                .build());
    }
}
