package ec.edu.ups.ppw.integrations;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class PythonServiceClient {

    private static final String PY_URL = "http://localhost:9000";
    private static final String API_KEY = "supersecreto123";

    private final HttpClient http = HttpClient.newHttpClient();

    public void sendEmail(String to, String subject, String text) {
        sendEmail(to, subject, text, null);
    }

    public void sendEmail(String to, String subject, String text, String replyTo) {
        if (to == null || to.isBlank()) return;

        StringBuilder json = new StringBuilder();
        json.append("{")
            .append("\"to\":\"").append(escape(to)).append("\",")
            .append("\"subject\":\"").append(escape(subject)).append("\",")
            .append("\"text\":\"").append(escape(text)).append("\"");

        if (replyTo != null && !replyTo.isBlank()) {
            json.append(",\"reply_to\":\"").append(escape(replyTo)).append("\"");
        }

        json.append("}");

        postJson("/notifications/email", json.toString());
    }

    public void sendTelegram(String chatId, String text) {
        if (chatId == null || chatId.isBlank()) return;

        String json = "{"
                + "\"chat_id\":\"" + escape(chatId) + "\","
                + "\"text\":\"" + escape(text) + "\""
                + "}";

        postJson("/notifications/telegram", json);
    }

    private void postJson(String path, String json) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(PY_URL + path))
                    .header("Content-Type", "application/json")
                    .header("X-API-KEY", API_KEY)
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() >= 400) {
                System.out.println("Python error " + resp.statusCode() + " (" + path + "): " + resp.body());
            }
        } catch (Exception e) {
            System.out.println("Python call failed (" + path + "): " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "");
    }
}
