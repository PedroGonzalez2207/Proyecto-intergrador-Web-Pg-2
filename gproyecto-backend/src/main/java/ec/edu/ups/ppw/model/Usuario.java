package ec.edu.ups.ppw.model;

import java.time.LocalDateTime;
import jakarta.persistence.*;

@Entity
@Table(name = "TBL_USUARIO",
       uniqueConstraints = {
           @UniqueConstraint(name="UK_USUARIO_EMAIL", columnNames="email"),
           @UniqueConstraint(name="UK_USUARIO_FIREBASE_UID", columnNames="firebase_uid")
       })
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Identificador estable del usuario Firebase
    @Column(name="firebase_uid", nullable=true, length=80)
    private String firebaseUid;

    @Column(nullable=false, length=120)
    private String email;

    // Para Firebase no aplica. Para login propio en el futuro sí.
    @Column(name="password_hash", nullable=true, length=200)
    private String passwordHash;

    @Column(nullable=false, length=120)
    private String nombres;

    // A veces no lo tendrás separado al inicio, así que lo permitimos null
    @Column(nullable=true, length=120)
    private String apellidos;

    @Column(length=30)
    private String telefono;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false, length=20)
    private Rol rol;

    @Column(nullable=false)
    private Boolean activo = true;

    @Column(name="created_at", nullable=false)
    private LocalDateTime createdAt;

    public Usuario() {}

    @PrePersist
    public void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (activo == null) {
            activo = true;
        }
    }

    public Long getId() { return id; }

    public String getFirebaseUid() { return firebaseUid; }
    public void setFirebaseUid(String firebaseUid) { this.firebaseUid = firebaseUid; }

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

    public Rol getRol() { return rol; }
    public void setRol(Rol rol) { this.rol = rol; }

    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }

    public LocalDateTime getCreatedAt() { return createdAt; }
}
