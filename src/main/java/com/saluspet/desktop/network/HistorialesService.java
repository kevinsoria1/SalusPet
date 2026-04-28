package com.saluspet.desktop.network;

import com.google.gson.Gson;
import com.saluspet.desktop.model.Historial;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

public class HistorialesService {

    private final HttpClient httpClient;
    private final Gson gson;

    public HistorialesService() {
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.gson = new Gson();
    }

    public CompletableFuture<Boolean> crearHistorialAsync(Historial historial) {
        String targetUrl = AuthService.API_BASE_URL.replace("/auth", "/HistorialClinico");
        String jsonPayload = gson.toJson(historial);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(targetUrl))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> response.statusCode() >= 200 && response.statusCode() < 300);
    }

    public CompletableFuture<java.util.List<Historial>> obtenerHistorialesMascotaAsync(int idMascota) {
        String targetUrl = AuthService.API_BASE_URL.replace("/auth", "/HistorialClinico/mascota/" + idMascota);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(targetUrl))
                .header("Accept", "application/json")
                .GET()
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() == 200) {
                        java.lang.reflect.Type listType = new com.google.gson.reflect.TypeToken<java.util.List<Historial>>(){}.getType();
                        return gson.fromJson(response.body(), listType);
                    }
                    return new java.util.ArrayList<>();
                });
    }
}
