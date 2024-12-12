package com.xtensus.xteged.web.rest;


import com.xtensus.xteged.service.AlfrescoService;
import com.xtensus.xteged.service.KeycloakUserSyncService;
import com.xtensus.xteged.service.PersonneRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/ged-controller")
public class UserrController {
    @Autowired
    private AlfrescoService alfrescoService; // Service pour gérer la création des utilisateurs dans Alfresco

    @Autowired
    private KeycloakUserSyncService keycloakUserSyncService; // Service pour synchroniser les utilisateurs avec Keycloak

    @PostMapping("/api/ged-controller/create-person")
    public Mono<ResponseEntity<String>> createPerson(@RequestBody PersonneRequest personRequest) {
        return alfrescoService.createPerson(personRequest) // Crée l'utilisateur dans Alfresco
            .flatMap(response -> {
                // Synchronisation avec Keycloak
                return keycloakUserSyncService.synchronizeUserToKeycloak(personRequest.getFirstName(), personRequest.getEmail())
                    .then(Mono.just(ResponseEntity.ok("Personne créée et synchronisée avec Keycloak.")))
                    .onErrorResume(e -> {
                        // Gestion des erreurs lors de la synchronisation avec Keycloak
                        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body("Personne créée dans Alfresco, mais erreur de synchronisation avec Keycloak: " + e.getMessage()));
                    });
            })
            .onErrorResume(e -> Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Erreur lors de la création de la personne: " + e.getMessage())));
    }}
