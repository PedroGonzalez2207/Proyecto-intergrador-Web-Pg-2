package ec.edu.ups.ppw.rest;

import java.util.HashSet;
import java.util.Set;

import ec.edu.ups.ppw.rest.filters.CORSFilter;
import ec.edu.ups.ppw.rest.filters.PreflightFilter;
import ec.edu.ups.ppw.rest.filters.JwtAuthFilter;
import ec.edu.ups.ppw.rest.filters.RoleAuthorizationFilter;
import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

@ApplicationPath("/api")
public class JaxRsActivator extends Application {

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> s = new HashSet<>();

        s.add(PreflightFilter.class);
        s.add(CORSFilter.class);
        s.add(JwtAuthFilter.class);
        s.add(RoleAuthorizationFilter.class);

        s.add(TestResource.class);
        s.add(WhoAmIResource.class);
        s.add(AdminUserResource.class);
        s.add(ProgramadorResource.class);
        s.add(AsesoriaResource.class);
        s.add(ProyectoResource.class);
        s.add(AdminProgramadorResource.class);

        return s;
    }
}
