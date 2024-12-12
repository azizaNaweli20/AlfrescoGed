package com.xtensus.xteged.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.http.MediaType;
import org.json.JSONObject;
import reactor.core.publisher.Mono;

import java.util.Map;


@Service
public class KeycloakUserSyncService {

    private final WebClient webClient;

    @Autowired
    public KeycloakUserSyncService(WebClient.Builder webClientBuilder) {
        // Utilisation de webClientBuilder pour construire un WebClient
        this.webClient = webClientBuilder
            .baseUrl("http://localhost:9080/auth/admin/realms/postarion")
            .build();
    }

    public Mono<Void> synchronizeUserToKeycloak(String username, String email) {
        return getKeycloakAdminToken("admin", "admin") // Remplacez par les informations d'identification appropriées
            .flatMap(token -> {
                // Préparer la requête JSON
                String userJson = String.format("{\"username\": \"%s\", \"email\": \"%s\", \"enabled\": true}", username, email);

                return webClient.post()
                    .uri("http://localhost:9080/auth/admin/realms/postarion/users") // Assurez-vous que l'URI est correcte
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromValue(userJson))
                    .retrieve()
                    .bodyToMono(Void.class) // Vous pouvez traiter la réponse si nécessaire
                    .onErrorResume(e -> {
                        // Gestion des erreurs lors de la synchronisation avec Keycloak
                        return Mono.error(new RuntimeException("Erreur de synchronisation avec Keycloak: " + e.getMessage()));
                    });
            });
    }

    public Mono<String> getKeycloakAdminToken(String username, String password) {
        return webClient.post()
            .uri("")
            .headers(headers -> headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED))
            .bodyValue("client_id=admin-cli" +
                "&username=" + username +
                "&password=" + password +
                "&grant_type=password")
            .retrieve()
            .bodyToMono(Map.class)
            .map(response -> (String) response.get("access_token"))
            .onErrorResume(e -> {
                // Gérer les erreurs ici
                return Mono.empty();
            });
    }
}





















