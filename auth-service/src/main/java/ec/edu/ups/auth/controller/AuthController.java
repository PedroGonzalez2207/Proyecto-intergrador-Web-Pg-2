package ec.edu.ups.auth.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import ec.edu.ups.auth.dto.LoginRequest;
import ec.edu.ups.auth.dto.LoginResponse;
import ec.edu.ups.auth.model.Usuario;
import ec.edu.ups.auth.repository.UsuarioRepository;
import ec.edu.ups.auth.service.JwtService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UsuarioRepository usuarioRepo;
    private final JwtService jwtService;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public AuthController(UsuarioRepository usuarioRepo, JwtService jwtService) {
        this.usuarioRepo = usuarioRepo;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {

        if (req == null || req.email == null || req.password == null || req.email.isBlank() || req.password.isBlank()) {
            return ResponseEntity.badRequest().body("Faltan campos");
        }

        Usuario u = usuarioRepo.findByEmailAndActivoTrue(req.email.trim())
                .orElse(null);

        if (u == null) {
            return ResponseEntity.status(401).body("Credenciales inválidas");
        }

        String stored = u.getPasswordHash();
        boolean ok;
        if (stored != null && stored.startsWith("$2")) {
            ok = encoder.matches(req.password, stored);
        } else {
            ok = req.password.equals(stored);
        }

        if (!ok) {
            return ResponseEntity.status(401).body("Credenciales inválidas");
        }

        String token = jwtService.generateToken(
                u.getEmail(),
                Map.of("id", u.getId(), "rol", u.getRol())
        );

        return ResponseEntity.ok(new LoginResponse(
                token,
                u.getId(),
                u.getEmail(),
                u.getRol(),
                u.getNombres(),
                u.getApellidos()
        ));
    }
}
