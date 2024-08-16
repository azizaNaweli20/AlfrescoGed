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

    public SiteService(WebClient webClient) {
        this.webClient = webClient;
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

    // Méthode pour supprimer un site
    public Mono<Void> deleteSite(String siteId, boolean permanent) {
        String url = String.format("%s/sites/%s?permanent=%b", alfrescoUrl, siteId, permanent);

        return webClient.delete()
            .uri(url)
            .header(HttpHeaders.AUTHORIZATION, "Basic " + Base64.getEncoder().encodeToString((alfrescoUser + ":" + alfrescoPass).getBytes()))
            .retrieve()
            .onStatus(HttpStatus::isError, response -> Mono.error(new RuntimeException("Failed to delete site")))
            .bodyToMono(Void.class);
    }
    // Méthode pour mettre à jour un site
    public Mono<String> updateSite(String siteId, SiteUpdateRequest siteUpdateRequest) {
        String url = String.format("%s/sites/%s", alfrescoUrl, siteId);

        return webClient.put()
            .uri(url)
            .header(HttpHeaders.AUTHORIZATION, "Basic " + Base64.getEncoder().encodeToString((alfrescoUser + ":" + alfrescoPass).getBytes()))
            .bodyValue(siteUpdateRequest)
            .retrieve()
            .onStatus(HttpStatus::isError, response -> Mono.error(new RuntimeException("Failed to update site")))
            .bodyToMono(String.class);
    }

    // Méthode pour créer une adhésion au site
    public Mono<String> createSiteMembership(String siteId, List<SiteMembership> memberships) {
        String url = String.format("%s/sites/%s/members", alfrescoUrl, siteId);

        return webClient.post()
            .uri(url)
            .header(HttpHeaders.AUTHORIZATION, "Basic " + Base64.getEncoder().encodeToString((alfrescoUser + ":" + alfrescoPass).getBytes()))
            .contentType(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromValue(memberships))
            .retrieve()
            .onStatus(HttpStatus::isError, response -> Mono.error(new RuntimeException("Failed to create site membership")))
            .bodyToMono(String.class);
    }

    public Mono<ResponseEntity<String>> deleteSiteMember(String siteId, String personId) {
        String url = String.format("%s/sites/%s/members/%s", alfrescoUrl, siteId, personId);

        return webClient.delete()
            .uri(url)
            .header(HttpHeaders.AUTHORIZATION, "Basic " + Base64.getEncoder().encodeToString((alfrescoUser + ":" + alfrescoPass).getBytes()))
            .retrieve()
            .onStatus(HttpStatus::isError, response -> Mono.error(new RuntimeException("Failed to delete site member")))
            .bodyToMono(String.class)
            .map(body -> ResponseEntity.ok(body)); // Réponse avec contenu
    }

}
