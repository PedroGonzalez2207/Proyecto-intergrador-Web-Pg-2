package ec.edu.ups.ppw.rest.security;

import java.security.Principal;

public class UserPrincipal implements Principal {

    private final String uid;
    private final String email;
    private final String name;
    private final String rol;

    public UserPrincipal(String uid, String email, String name, String rol) {
        this.uid = uid;
        this.email = email;
        this.name = name;
        this.rol = rol;
    }

    @Override
    public String getName() {
        return uid;
    }

    public String getUid() { return uid; }
    public String getEmail() { return email; }
    public String getDisplayName() { return name; }
    public String getRol() { return rol; }
}
