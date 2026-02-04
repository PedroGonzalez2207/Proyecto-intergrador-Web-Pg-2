package ec.edu.ups.ppw.rest;

import jakarta.ws.rs.OPTIONS;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

@Path("{path: .*}")
public class CorsPreflightResource {

    @OPTIONS
    public Response preflight() {
        return Response.ok().build();
    }
}
