package ec.edu.ups.auth.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;

import ec.edu.ups.auth.dto.FirebaseLoginRequest;
import ec.edu.ups.auth.dto.LoginResponse;
import ec.edu.ups.auth.service.JwtService;

@RestController
@RequestMapping("/api/auth")
public class FirebaseVerifyController {

  private final JwtService jwtService;

  public FirebaseVerifyController(JwtService jwtService) {
    this.jwtService = jwtService;
  }

  @PostMapping("/firebase")
  public ResponseEntity<?> loginFirebase(@RequestBody FirebaseLoginRequest req) {

    if (req == null || req.idToken == null || req.idToken.isBlank()) {
      return ResponseEntity.badRequest().body("Falta idToken");
    }

    try {
      FirebaseToken decoded = FirebaseAuth.getInstance().verifyIdToken(req.idToken.trim());

      String uid = decoded.getUid();
      String email = decoded.getEmail() != null ? decoded.getEmail() : "";
      String name = decoded.getName() != null ? decoded.getName() : "";

      String rol = "Usuario";

      String jwt = jwtService.generateToken(
    		  email.isBlank() ? uid : email,
    		  Map.of("uid", uid, "email", email, "rol", rol, "name", name)
    		);
      
      return ResponseEntity.ok(new LoginResponse(
        jwt,
        null,
        email,
        rol,
        name,
        ""
      ));

    } catch (Exception e) {
      return ResponseEntity.status(401).body("Firebase token inválido");
    }
  }
}
