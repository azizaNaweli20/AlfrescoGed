package com.xtensus.xteged.service;


import com.xtensus.xteged.service.impl.CmisServiceImpl;
import com.xtensus.xteged.web.rest.vm.SiteUpdateRequest;
import org.apache.chemistry.opencmis.client.api.*;
import org.apache.chemistry.opencmis.client.runtime.SessionFactoryImpl;
import org.apache.chemistry.opencmis.commons.SessionParameter;

import org.apache.chemistry.opencmis.commons.enums.BindingType;

import org.springframework.beans.factory.annotation.Value;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;


import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;


@Service
public class SiteService {

    private Session session;

    private final WebClient webClient;

    @Value("${alfresco.repository.user}")
    private String alfrescoUser;
    @Value("${alfresco.url}")
    private String alfrescoUrl;
    @Value("${alfresco.repository.pass}")
    private String alfrescoPass;

    @Value("${alfresco.repository.url}")
    private String alfrescoRepoUrl;



    private static final HashMap<String, String> mimeTypeMapping = new HashMap<>();
    private final Logger log = LoggerFactory.getLogger(CmisServiceImpl.class);


    public SiteService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("http://localhost:8081").build(); // Assurez-vous de définir l'URL de base appropriée
    }


    // Méthode pour initialiser la session Alfresco
    public void setSession(Session session) {
        this.session = session;
    }

    public void initializeSession(String username, String password, String atomPubUrl) {
        Map<String, String> parameters = new HashMap<>();
        parameters.put(SessionParameter.USER, username);
        parameters.put(SessionParameter.PASSWORD, password);
        parameters.put(SessionParameter.ATOMPUB_URL, atomPubUrl);
        parameters.put(SessionParameter.BINDING_TYPE, BindingType.ATOMPUB.value());

        SessionFactory sessionFactory = SessionFactoryImpl.newInstance();
        this.session = sessionFactory.getRepositories(parameters).get(0).createSession();
    }

    ///////////////////////// Méthode pour obtenir la liste des sites
    public Mono<String> getSites(String visibility, String orderBy, int skipCount, int maxItems) {
        String url = String.format("%s/sites?skipCount=%d&maxItems=%d", alfrescoUrl, skipCount, maxItems);
        StringBuilder uriBuilder = new StringBuilder(url);

        if (visibility != null && !visibility.isEmpty()) {
            uriBuilder.append("&where=(visibility='").append(visibility).append("')");
        }
        if (orderBy != null && !orderBy.isEmpty()) {
            uriBuilder.append("&orderBy=").append(orderBy);
        }

        return webClient.get()
            .uri(uriBuilder.toString())
            .header(HttpHeaders.AUTHORIZATION, "Basic " + Base64.getEncoder().encodeToString((alfrescoUser + ":" + alfrescoPass).getBytes()))
            .retrieve()
            .onStatus(HttpStatus::isError, response -> {
                log.error("Failed to retrieve sites. Status code: {}", response.statusCode());
                return response.bodyToMono(String.class)
                    .flatMap(body -> Mono.error(new RuntimeException("Failed to retrieve sites: " + body)));
            })
            .bodyToMono(String.class);
    }
     //////////////////createSite///////////////
    public Mono<String> createSite(String title, String visibility, String description, boolean skipConfiguration, boolean skipAddToFavorites) {
        String url = String.format("%s/sites?skipConfiguration=%b&skipAddToFavorites=%b", alfrescoUrl, skipConfiguration, skipAddToFavorites);

        Map<String, String> siteDetails = new HashMap<>();
        siteDetails.put("title", title);
        siteDetails.put("visibility", visibility);
        siteDetails.put("description", description);

        return webClient.post()
            .uri(url)
            .header(HttpHeaders.AUTHORIZATION, "Basic " + Base64.getEncoder().encodeToString((alfrescoUser + ":" + alfrescoPass).getBytes()))
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .body(BodyInserters.fromValue(siteDetails))
            .retrieve()
            .onStatus(HttpStatus::isError, response -> Mono.error(new RuntimeException("Failed to create site")))
            .bodyToMono(String.class);
    }

    public Mono<Void> deleteSite(String siteId, boolean permanent) {
        String url = String.format("%s/sites/%s?permanent=%b", alfrescoUrl, siteId, permanent);

        return webClient.delete()
            .uri(url)
            .header(HttpHeaders.AUTHORIZATION, "Basic " + Base64.getEncoder().encodeToString((alfrescoUser + ":" + alfrescoPass).getBytes()))
            .retrieve()
            .onStatus(HttpStatus::isError, response -> {
                // Log de l'erreur pour plus de détails
                return response.bodyToMono(String.class)
                    .flatMap(errorBody -> {
                        // Log du corps de la réponse en cas d'erreur
                        System.err.println("Erreur lors de la suppression du site : " + errorBody);
                        return Mono.error(new RuntimeException("Failed to delete site: " + errorBody));
                    });
            })
            .bodyToMono(Void.class);
    }

    // Méthode pour mettre à jour un site
    public Mono<SiteResponse> updateSite(String siteId, SiteUpdateRequest siteUpdateRequest) {
        String url = String.format("%s/sites/%s", alfrescoUrl, siteId);

        return webClient.put()
            .uri(url)
            .header(HttpHeaders.AUTHORIZATION, "Basic " + Base64.getEncoder().encodeToString((alfrescoUser + ":" + alfrescoPass).getBytes()))
            .bodyValue(siteUpdateRequest)
            .retrieve()
            .onStatus(HttpStatus::isError, response -> Mono.error(new RuntimeException("Failed to update site")))
            .bodyToMono(SiteResponse.class);
    }

    // Méthode pour ajouter un membre à un site
    public Mono<String> addMemberToSite(String siteId, String id, String role) {
        String url = String.format("%s/sites/%s/members", alfrescoUrl, siteId);

        Map<String, String> memberDetails = new HashMap<>();
        memberDetails.put("id", id);
        memberDetails.put("role", role);

        return webClient.post()
            .uri(url)
            .header(HttpHeaders.AUTHORIZATION, "Basic " + Base64.getEncoder().encodeToString((alfrescoUser + ":" + alfrescoPass).getBytes()))
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .body(BodyInserters.fromValue(memberDetails))
            .retrieve()
            .onStatus(HttpStatus::isError, response -> {
                // Log de l'erreur pour plus de détails
                return response.bodyToMono(String.class)
                    .flatMap(errorBody -> {
                        // Log du corps de la réponse en cas d'erreur
                        System.err.println("Erreur lors de l'ajout du membre : " + errorBody);
                        return Mono.error(new RuntimeException("Failed to add member: " + errorBody));
                    });
            })
            .bodyToMono(String.class);
    }


    public Mono<Void> deleteSiteMember(String siteId, String personId) {
        String url = String.format("%s/sites/%s/members/%s", alfrescoUrl, siteId, personId);

        return webClient.delete()
            .uri(url)
            .header(HttpHeaders.AUTHORIZATION, "Basic " + Base64.getEncoder().encodeToString((alfrescoUser + ":" + alfrescoPass).getBytes()))
            .retrieve()
            .onStatus(HttpStatus::isError, response -> {
                return response.bodyToMono(String.class)
                    .flatMap(errorBody -> {
                        System.err.println("Erreur lors de la suppression du membre : " + errorBody);
                        return Mono.error(new RuntimeException("Failed to delete site member: " + errorBody));
                    });
            })
            .bodyToMono(Void.class);
    }


    public Mono<SiteResponse> getSiteInfo(String siteId, List<String> relations, List<String> fields) {
        String url = String.format("%s/sites/%s", alfrescoUrl, siteId);

        // Construction des paramètres de requête
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(url);

        if (relations != null && !relations.isEmpty()) {
            uriBuilder.queryParam("relations", String.join(",", relations));
        }
        if (fields != null && !fields.isEmpty()) {
            uriBuilder.queryParam("fields", String.join(",", fields));
        }

        return webClient.get()
            .uri(uriBuilder.toUriString())
            .header(HttpHeaders.AUTHORIZATION, "Basic " + Base64.getEncoder().encodeToString((alfrescoUser + ":" + alfrescoPass).getBytes()))
            .retrieve()
            .onStatus(HttpStatus::isError, response -> Mono.error(new RuntimeException("Failed to get site information")))
            .bodyToMono(SiteResponse.class);
    }}
