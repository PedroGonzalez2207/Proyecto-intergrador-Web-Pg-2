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

    // TODO: PON AQUI TU CORREO ADMIN
	private static final String ADMIN_EMAIL = "pedrojose.g2207@gmail.com";

    @Inject
    private UsuarioDAO usuarioDAO;

    @PostConstruct
    public void init() {
        if (ADMIN_EMAIL == null || ADMIN_EMAIL.isBlank() || ADMIN_EMAIL.contains("pedrojose.g2207@gmail.com")) {
            System.out.println("[AdminSeed] ADMIN_EMAIL no configurado. No se crea admin.");
            return;
        }

        Usuario existing = usuarioDAO.findByEmail(ADMIN_EMAIL);
        if (existing != null) {
            // Si ya existe, asegurar que sea ADMIN
            if (existing.getRol() != Rol.Admin) {
                existing.setRol(Rol.Admin);
                usuarioDAO.update(existing);
                System.out.println("[AdminSeed] Usuario existente promovido a ADMIN: " + ADMIN_EMAIL);
            } else {
                System.out.println("[AdminSeed] Admin ya existe: " + ADMIN_EMAIL);
            }
            return;
        }

        Usuario admin = new Usuario();
        admin.setEmail(ADMIN_EMAIL);
        admin.setRol(Rol.Admin);
        admin.setNombres("Admin");
        admin.setApellidos("Sistema");
        admin.setActivo(true);

        // firebaseUid queda NULL hasta que ese correo haga login por Firebase y pase por /api/me
        admin.setFirebaseUid(null);

        usuarioDAO.create(admin);
        System.out.println("[AdminSeed] Admin creado: " + ADMIN_EMAIL);
    }
}
