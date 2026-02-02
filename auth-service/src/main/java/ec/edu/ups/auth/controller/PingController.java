package ec.edu.ups.auth.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PingController {

    @GetMapping("/api/auth/ping")
    public String ping() {
        return "OK auth-service";
    }
}
