package ec.edu.ups.ppw.rest.dto;

import ec.edu.ups.ppw.model.Rol;

public class RegisterRequest {
    public String email;
    public String password;
    public String nombres;
    public String apellidos;
    public String telefono;
    public Rol rol; 
}
