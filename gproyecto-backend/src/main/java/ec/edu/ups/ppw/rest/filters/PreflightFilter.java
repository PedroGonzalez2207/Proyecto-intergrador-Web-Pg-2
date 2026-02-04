package ec.edu.ups.ppw.rest.filters;

import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import jakarta.ws.rs.container.PreMatching;

@Provider
@PreMatching
@Priority(Priorities.AUTHENTICATION - 200)
public class PreflightFilter implements ContainerRequestFilter {

    @Override
    public void filter(ContainerRequestContext ctx) {
        if (!"OPTIONS".equalsIgnoreCase(ctx.getMethod())) return;

        String origin = ctx.getHeaderString("Origin");
        if (origin == null || origin.isBlank()) origin = "http://localhost:4200";

        String reqHeaders = ctx.getHeaderString("Access-Control-Request-Headers");
        if (reqHeaders == null || reqHeaders.isBlank()) {
            reqHeaders = "Origin,Content-Type,Accept,Authorization";
        }

        ctx.abortWith(
            Response.ok()
                .header("Access-Control-Allow-Origin", origin)
                .header("Vary", "Origin")
                .header("Access-Control-Allow-Credentials", "true")
                .header("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,OPTIONS")
                .header("Access-Control-Allow-Headers", reqHeaders)
                .header("Access-Control-Max-Age", "3600")
                .build()
        );
    }
}
