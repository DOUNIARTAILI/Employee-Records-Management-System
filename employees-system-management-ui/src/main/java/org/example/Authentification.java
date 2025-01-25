package org.example;

import com.google.gson.JsonObject;
import com.google.gson.Gson;
import javax.swing.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class Authentification {
    private static String jwtToken;

    public static void sendAuthRequest(JsonObject requestBody, String endpoint, Consumer<String> onSuccess) {
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {

                String jsonInput = new Gson().toJson(requestBody);

                URL url = new URL("http://localhost:8080/api/v1/auth/" + endpoint);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                // Send request
                try (OutputStream os = conn.getOutputStream()) {
                    byte[] input = jsonInput.getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }

                // Handle response
                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                        String response = br.lines().collect(Collectors.joining());
                        JsonObject jsonResponse = new Gson().fromJson(response, JsonObject.class);
                        String token = jsonResponse.get("token").getAsString();
                        if ("register".equals(endpoint)) {
                            onSuccess.accept("");
                        } else {
                            jwtToken = token;
                            String role = jsonResponse.get("role").getAsString();
                            onSuccess.accept(role);
                        }
                    }
                } else {
                    try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8))) {
                        String errorResponse = br.lines().collect(Collectors.joining());
                        JOptionPane.showMessageDialog(null, "Error: " + errorResponse, "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
                return null;
            }
        };
        worker.execute();
    }

    public static String getJwtToken() {
        return jwtToken;
    }
}