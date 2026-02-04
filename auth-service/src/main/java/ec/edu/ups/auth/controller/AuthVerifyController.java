package ec.edu.ups.auth.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import ec.edu.ups.auth.service.JwtVerifyService;

@RestController
@RequestMapping("/api/auth")
public class AuthVerifyController {

  private final JwtVerifyService verify;

  public AuthVerifyController(JwtVerifyService verify) {
    this.verify = verify;
  }

  @GetMapping("/verify")
  public ResponseEntity<?> verify(@RequestHeader("Authorization") String auth) {
    return verify.verify(auth);
  }
}
