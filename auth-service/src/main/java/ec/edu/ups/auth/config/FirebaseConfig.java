package ec.edu.ups.auth.config;

import java.io.IOException;
import java.io.InputStream;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import jakarta.annotation.PostConstruct;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

@Configuration
public class FirebaseConfig {

  @PostConstruct
  public void initFirebase() throws IOException {
    if (!FirebaseApp.getApps().isEmpty()) return;

    ClassPathResource resource = new ClassPathResource("proyecto-integrador-web-pg-firebase-adminsdk-fbsvc-59ec0edf73.json");

    try (InputStream is = resource.getInputStream()) {
      FirebaseOptions options = FirebaseOptions.builder()
        .setCredentials(GoogleCredentials.fromStream(is))
        .build();
      FirebaseApp.initializeApp(options);
    }
  }
}

