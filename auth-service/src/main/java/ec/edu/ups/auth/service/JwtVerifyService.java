package ec.edu.ups.auth.service;

import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtVerifyService {

  @Value("${jwt.secret}")
  private String secret;

  public ResponseEntity<?> verify(String authHeader) {
    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
      return ResponseEntity.status(401).body("Missing Bearer token");
    }

    String token = authHeader.substring("Bearer ".length()).trim();

    try {
      Claims claims = Jwts.parserBuilder()
          .setSigningKey(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)))
          .build()
          .parseClaimsJws(token)
          .getBody();

      String uid = claims.get("uid", String.class);
      String email = claims.get("email", String.class);
      String name = claims.get("name", String.class);

      return ResponseEntity.ok(
          java.util.Map.of(
              "uid", uid == null ? "" : uid,
              "email", email == null ? "" : email,
              "name", name == null ? "" : name
          )
      );

    } catch (Exception e) {
      return ResponseEntity.status(401).body("Invalid or expired token");
    }
  }
}

