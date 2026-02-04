package ec.edu.ups.ppw.rest.filters;

import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;

@Provider
@Priority(Priorities.HEADER_DECORATOR)
public class CORSFilter implements ContainerResponseFilter {

    @Override
    public void filter(ContainerRequestContext req, ContainerResponseContext res) {
        res.getHeaders().putSingle("Access-Control-Allow-Origin", "http://localhost:4200");
        res.getHeaders().putSingle("Vary", "Origin");
        res.getHeaders().putSingle("Access-Control-Allow-Credentials", "true");
        res.getHeaders().putSingle("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,OPTIONS");
        res.getHeaders().putSingle("Access-Control-Allow-Headers", "Origin,Content-Type,Accept,Authorization");
        res.getHeaders().putSingle("Access-Control-Expose-Headers", "Authorization");
    }
}
