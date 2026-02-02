package ec.edu.ups.auth.dto;

public class LoginResponse {
    public String token;
    public Long id;
    public String email;
    public String rol;
    public String nombres;
    public String apellidos;

    public LoginResponse(String token, Long id, String email, String rol, String nombres, String apellidos) {
        this.token = token;
        this.id = id;
        this.email = email;
        this.rol = rol;
        this.nombres = nombres;
        this.apellidos = apellidos;
    }
}
