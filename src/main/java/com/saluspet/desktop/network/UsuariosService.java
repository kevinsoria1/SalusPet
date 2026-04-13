package com.saluspet.desktop.network;

import com.google.gson.Gson;
import com.saluspet.desktop.model.Usuario;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

public class UsuariosService {

    private final HttpClient httpClient;
    private final Gson gson;

    public UsuariosService() {
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.gson = new Gson();
    }

    public CompletableFuture<Usuario> obtenerUsuarioAsync(int idUsuario) {
        String targetUrl = AuthService.API_BASE_URL.replace("/auth", "/Usuarios/") + idUsuario;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(targetUrl))
                .header("Accept", "application/json")
                .GET()
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() == 200) {
                        return gson.fromJson(response.body(), Usuario.class);
                    } else {
                        throw new RuntimeException("Error obteniendo usuario. HTTP " + response.statusCode());
                    }
                });
    }
}
