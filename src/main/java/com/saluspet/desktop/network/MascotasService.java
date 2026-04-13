package com.saluspet.desktop.network;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.saluspet.desktop.model.Mascota;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Servicio encargado de gestionar las peticiones REST contra el controlador de
 * Pacientes/Mascotas.
 */
public class MascotasService {

    private final HttpClient httpClient;
    private final Gson gson;

    public MascotasService() {
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.gson = new Gson();
    }

    /**
     * Trae de golpe todas las mascotas activas (GET puro).
     */
    public CompletableFuture<List<Mascota>> obtenerMascotasAsync() {
        // Exigencia del backend: usar el endpoint extendido
        String targetUrl = AuthService.API_BASE_URL.replace("/auth", "/Mascotas");

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(targetUrl))
                .header("Accept", "application/json")
                .GET()
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    System.out.println("== RESPUESTA MASCOTAS ==");
                    System.out.println("CODE: " + response.statusCode());
                    System.out.println("BODY: " + response.body());
                    if (response.statusCode() == 200) {
                        // Magia reflexiva de Gson para parsear un Array JSON a una Lista estricta de
                        // objetos Mascota
                        return gson.fromJson(response.body(), new TypeToken<List<Mascota>>() {
                        }.getType());
                    } else {
                        throw new RuntimeException("Error en servidor. Código devuelto: " + response.statusCode());
                    }
                });
    }
}
