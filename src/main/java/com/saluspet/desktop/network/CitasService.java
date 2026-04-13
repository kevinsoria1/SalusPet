package com.saluspet.desktop.network;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.saluspet.desktop.model.Cita;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Servicio encargado de gestionar las peticiones REST contra el controlador de
 * Citas.
 */
public class CitasService {

    private final HttpClient httpClient;
    private final Gson gson;

    public CitasService() {
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.gson = new com.google.gson.GsonBuilder().serializeNulls().create();
    }

    /**
     * Trae de golpe todas las citas (GET puro).
     */
    public CompletableFuture<List<Cita>> obtenerCitasAsync() {
        // Exigencia del backend: usar el endpoint extendido
        String targetUrl = AuthService.API_BASE_URL.replace("/auth", "/Citas");

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(targetUrl))
                .header("Accept", "application/json")
                .GET()
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    System.out.println("== RESPUESTA CITAS ==");
                    System.out.println("CODE: " + response.statusCode());
                    System.out.println("BODY: " + response.body());
                    if (response.statusCode() == 200) {
                        return gson.fromJson(response.body(), new TypeToken<List<Cita>>() {
                        }.getType());
                    } else {
                        throw new RuntimeException("Error en servidor. Código devuelto: " + response.statusCode());
                    }
                });
    }

    public CompletableFuture<List<Cita>> obtenerCitasSinAsignarAsync() {
        String targetUrl = AuthService.API_BASE_URL.replace("/auth", "/Citas/pendientes");

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(targetUrl))
                .header("Accept", "application/json")
                .GET()
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() == 200) {
                        return gson.fromJson(response.body(), new TypeToken<List<Cita>>() {
                        }.getType());
                    } else {
                        throw new RuntimeException("Error al obtener citas sin asignar: " + response.statusCode());
                    }
                });
    }

    public CompletableFuture<RespuestaPaginadaCitas> obtenerCitasVeterinarioConfirmadasAsync(int idVeterinario) {
        String targetUrl = AuthService.API_BASE_URL.replace("/auth", "/Citas/veterinario/") + idVeterinario + "?estado=Confirmada&page=1&pageSize=10";
        System.out.println("Llamando a (CONFIRMADAS): " + targetUrl);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(targetUrl))
                .header("Accept", "application/json")
                .GET()
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    System.out.println("Respuesta API CONFIRMADAS: " + response.body());
                    if (response.statusCode() == 200) {
                        return gson.fromJson(response.body(), RespuestaPaginadaCitas.class);
                    } else {
                        throw new RuntimeException("Error al obtener citas confirmadas: " + response.statusCode());
                    }
                });
    }

    public CompletableFuture<RespuestaPaginadaCitas> obtenerCitasVeterinarioHistorialAsync(int idVeterinario, int page) {
        String targetUrl = AuthService.API_BASE_URL.replace("/auth", "/Citas/veterinario/") + idVeterinario + "?estado=Completada&page=" + page + "&pageSize=10";
        System.out.println("Llamando a (HISTORIAL): " + targetUrl);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(targetUrl))
                .header("Accept", "application/json")
                .GET()
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() == 200) {
                        return gson.fromJson(response.body(), RespuestaPaginadaCitas.class);
                    } else {
                        throw new RuntimeException("Error al obtener historial: " + response.statusCode());
                    }
                });
    }

    public CompletableFuture<Cita> obtenerCitaPorIdAsync(int idCita) {
        String targetUrl = AuthService.API_BASE_URL.replace("/auth", "/Citas/") + idCita;
        System.out.println("Llamando a (GET UNITARIO): " + targetUrl);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(targetUrl))
                .header("Accept", "application/json")
                .GET()
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() == 200) {
                        return gson.fromJson(response.body(), Cita.class);
                    } else {
                        throw new RuntimeException("Error al obtener la cita por ID: " + response.statusCode());
                    }
                });
    }

    public CompletableFuture<Boolean> actualizarCitaAsync(Cita cita) {
        String targetUrl = AuthService.API_BASE_URL.replace("/auth", "/Citas/") + cita.getIdCita();
        
        String jsonPayload = gson.toJson(cita);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(targetUrl))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> response.statusCode() >= 200 && response.statusCode() < 300);
    }
}
