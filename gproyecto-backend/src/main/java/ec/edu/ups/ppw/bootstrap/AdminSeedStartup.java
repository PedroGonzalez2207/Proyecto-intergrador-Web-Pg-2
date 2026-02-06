package ec.edu.ups.ppw.bootstrap;

import ec.edu.ups.ppw.dao.UsuarioDAO;
import ec.edu.ups.ppw.model.Rol;
import ec.edu.ups.ppw.model.Usuario;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import jakarta.inject.Inject;

@Singleton
@Startup
public class AdminSeedStartup {

    private static final String ADMIN_EMAIL = "pedrojose.g2207@gmail.com";

    @Inject
    private UsuarioDAO usuarioDAO;

    @PostConstruct
    public void init() {
        if (ADMIN_EMAIL == null || ADMIN_EMAIL.isBlank()) {
            System.out.println("[AdminSeed] ADMIN_EMAIL no configurado. No se crea admin.");
            return;
        }

        String email = ADMIN_EMAIL.trim().toLowerCase();

        Usuario existing = usuarioDAO.findByEmail(email);
        if (existing != null) {
            if (existing.getRol() != Rol.Admin) {
                existing.setRol(Rol.Admin);
                usuarioDAO.update(existing);
                System.out.println("[AdminSeed] Usuario existente promovido a Admin: " + email);
            } else {
                System.out.println("[AdminSeed] Admin ya existe: " + email);
            }
            return;
        }

        Usuario admin = new Usuario();
        admin.setEmail(email);
        admin.setRol(Rol.Admin);
        admin.setNombres("Admin");
        admin.setApellidos("Sistema");
        admin.setActivo(true);
        admin.setFirebaseUid(null);

        usuarioDAO.insert(admin);
        System.out.println("[AdminSeed] Admin creado: " + email);
    }
}
