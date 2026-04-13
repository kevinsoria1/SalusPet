package com.saluspet.desktop.network;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

public class AuthService {

    // URL activa de DevTunnels aportada por Aarón
    public static final String API_BASE_URL = "https://3dvd8r44-7248.uks1.devtunnels.ms/api/auth"; 
    
    private final HttpClient httpClient;
    private final Gson gson;

    public AuthService() {
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .connectTimeout(Duration.ofSeconds(10))
                .build();
                
        this.gson = new Gson();
    }

    public CompletableFuture<HttpResponse<String>> loginAsync(String email, String password) {
        String jsonPayload = gson.toJson(new LoginDto(email, password));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_BASE_URL + "/login"))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());
    }

    public CompletableFuture<HttpResponse<String>> registerAsync(String email, String password, String codigoClinica, String nombre, String especialidad, String numeroColegiado) {
        String jsonPayload = gson.toJson(new RegisterDto(email, password, codigoClinica, nombre, especialidad, numeroColegiado));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_BASE_URL + "/register"))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());
    }

    // --- DTOs Privados adaptados al nuevo contrato ---
    
    private static class LoginDto {
        @SerializedName("Email")
        String email;
        @SerializedName("Password")
        String password;
        LoginDto(String email, String password) { 
            this.email = email; 
            this.password = password; 
        }
    }

    private static class RegisterDto {
        @SerializedName("Email")
        String email;
        @SerializedName("Password")
        String password;
        @SerializedName("CodigoClinica")
        String codigoClinica;
        @SerializedName("Nombre")
        String nombre;
        @SerializedName("Especialidad")
        String especialidad;
        @SerializedName("NumeroColegiado")
        String numeroColegiado;
        
        RegisterDto(String email, String password, String codigoClinica, String nombre, String especialidad, String numeroColegiado) { 
            this.email = email; 
            this.password = password; 
            this.codigoClinica = codigoClinica; 
            this.nombre = nombre;
            this.especialidad = especialidad;
            this.numeroColegiado = numeroColegiado;
        }
    }
}
