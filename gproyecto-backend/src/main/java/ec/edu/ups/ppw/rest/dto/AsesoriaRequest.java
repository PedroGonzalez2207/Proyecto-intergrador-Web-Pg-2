package ec.edu.ups.ppw.rest.dto;

import ec.edu.ups.ppw.model.Modalidad;

public class AsesoriaRequest {
    public Long clienteId;
    public Long programadorId;
    public String fechaInicio; 
    public String fechaFin;    
    public Modalidad modalidad;
}
