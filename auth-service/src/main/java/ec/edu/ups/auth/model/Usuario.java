package ec.edu.ups.auth.model;

import java.time.LocalDateTime;
import jakarta.persistence.*;

@Entity
@Table(name = "TBL_USUARIO")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false, length=120)
    private String email;

    @Column(name="password_hash", nullable=false, length=200)
    private String passwordHash;

    @Column(nullable=false, length=120)
    private String nombres;

    @Column(nullable=false, length=120)
    private String apellidos;

    @Column(length=30)
    private String telefono;

    @Column(nullable=false, length=20)
    private String rol; // viene como STRING en la tabla

    @Column(nullable=false)
    private Boolean activo = true;

    @Column(name="created_at", nullable=false)
    private LocalDateTime createdAt;

    // ===== getters/setters =====
    public Long getId() { return id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getNombres() { return nombres; }
    public void setNombres(String nombres) { this.nombres = nombres; }

    public String getApellidos() { return apellidos; }
    public void setApellidos(String apellidos) { this.apellidos = apellidos; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }

    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
