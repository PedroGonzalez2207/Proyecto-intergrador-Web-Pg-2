package ec.edu.ups.auth.config;

import java.io.FileInputStream;
import java.io.InputStream;

import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

@Configuration
public class FirebaseConfig {

  @PostConstruct
  public void init() throws Exception {
    if (!FirebaseApp.getApps().isEmpty()) return;

    String path = System.getenv("GOOGLE_APPLICATION_CREDENTIALS");
    if (path == null || path.isBlank()) {
      throw new IllegalStateException("Falta GOOGLE_APPLICATION_CREDENTIALS (ruta al JSON de Firebase Admin)");
    }

    try (InputStream is = new FileInputStream(path)) {
      FirebaseOptions options = FirebaseOptions.builder()
        .setCredentials(GoogleCredentials.fromStream(is))
        .build();
      FirebaseApp.initializeApp(options);
      System.out.println("[FirebaseConfig] Firebase Admin inicializado OK");
    }
  }
}
