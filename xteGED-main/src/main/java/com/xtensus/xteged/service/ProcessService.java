package com.xtensus.xteged.service;

import org.springframework.stereotype.Service;
import org.apache.chemistry.opencmis.client.api.*;
import org.apache.chemistry.opencmis.client.runtime.SessionFactoryImpl;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.enums.BindingType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;

import java.util.*;

import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
@Service
public class ProcessService {
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


    public ProcessService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("http://localhost:8081").build(); // Assurez-vous de définir l'URL de base appropriée
    }

    public Mono<String> getProcesses(int skipCount, int maxItems, String[] properties, String[] orderBy, String where) {
        StringBuilder uriBuilder = new StringBuilder(String.format("%s/workflow/versions/1/processes", alfrescoUrl));
        uriBuilder.append(String.format("?skipCount=%d&maxItems=%d", skipCount, maxItems));

        if (properties != null && properties.length > 0) {
            uriBuilder.append("&properties=" + String.join(",", properties));
        }
        if (orderBy != null && orderBy.length > 0) {
            uriBuilder.append("&orderBy=" + String.join(",", orderBy));
        }
        if (where != null && !where.isEmpty()) {
            uriBuilder.append("&where=" + where);
        }

        return webClient.get()
            .uri(uriBuilder.toString())
            .header(HttpHeaders.AUTHORIZATION, "Basic " + Base64.getEncoder().encodeToString((alfrescoUser + ":" + alfrescoPass).getBytes()))
            .header(HttpHeaders.ACCEPT, "application/json")
            .retrieve()
            .onStatus(HttpStatus::isError, response ->
                response.bodyToMono(String.class)
                    .flatMap(body -> Mono.error(new RuntimeException("Failed to get processes: " + body)))
            )
            .bodyToMono(String.class);
    }





    // Méthode pour initialiser la session Alfresco
    public void initializeSession(String username, String password, String atomPubUrl) {
        Map<String, String> parameters = new HashMap<>();
        parameters.put(SessionParameter.USER, username);
        parameters.put(SessionParameter.PASSWORD, password);
        parameters.put(SessionParameter.ATOMPUB_URL, atomPubUrl);
        parameters.put(SessionParameter.BINDING_TYPE, BindingType.ATOMPUB.value());

        SessionFactory sessionFactory = SessionFactoryImpl.newInstance();
        this.session = sessionFactory.getRepositories(parameters).get(0).createSession();
    }



    // Méthode pour supprimer un processus
    public Mono<Void> deleteProcess(String processId) {
        String url = String.format("%s/workflow/versions/1/processes/%s", alfrescoUrl, processId);

        return webClient.delete()
            .uri(url)
            .header(HttpHeaders.AUTHORIZATION, "Basic " + Base64.getEncoder().encodeToString((alfrescoUser + ":" + alfrescoPass).getBytes()))
            .header(HttpHeaders.ACCEPT, "application/json")
            .retrieve()
            .onStatus(HttpStatus::isError, response ->
                response.bodyToMono(String.class)
                    .flatMap(body -> Mono.error(new RuntimeException("Failed to delete process: " + body)))
            )
            .bodyToMono(Void.class);
    }


    // Méthode pour créer un processus
    public Mono<String> createProcess(String processDefinitionKey, ProcessVariables variables) {
        if (session == null) {
            throw new IllegalStateException("Session not initialized");
        }

        String requestBody = String.format(
            "{ \"processDefinitionKey\": \"%s\", \"variables\": { \"bpm_assignee\": \"%s\", \"bpm_sendEMailNotifications\": %b, \"bpm_workflowPriority\": %d } }",
            processDefinitionKey,
            variables.getBpm_assignee(),
            variables.isBpm_sendEMailNotifications(),
            variables.getBpm_workflowPriority()
        );

        return webClient.post()
            .uri(String.format("%s/alfresco/api/-default-/public/workflow/versions/1/processes", alfrescoUrl))
            .header(HttpHeaders.AUTHORIZATION, "Basic " + Base64.getEncoder().encodeToString((alfrescoUser + ":" + alfrescoPass).getBytes()))
            .header(HttpHeaders.CONTENT_TYPE, "application/json")
            .bodyValue(requestBody)
            .retrieve()
            .onStatus(HttpStatus::isError, response ->
                response.bodyToMono(String.class)
                    .flatMap(body -> Mono.error(new RuntimeException("Failed to create process: " + body)))
            )
            .bodyToMono(String.class);
    }

/*
    public Mono<String> startWorkflow(String processDefinitionKey, WorkflowVariables variables) {
        String requestBody = String.format(
            "{ \"processDefinitionKey\": \"%s\", \"variables\": { \"bpm_assignee\": \"%s\", \"bpm_sendEMailNotifications\": %b, \"bpm_workflowPriority\": %d } }",
            processDefinitionKey,
            variables.getBpm_assignee(),
            variables.isBpm_sendEMailNotifications(),
            variables.getBpm_workflowPriority()
        );

        return webClient.post()
            .uri("/alfresco/api/-default-/public/workflow/versions/1/processes")
            .header("Authorization", "Basic " + java.util.Base64.getEncoder().encodeToString((alfrescoUser + ":" + alfrescoPass).getBytes()))
            .header("Content-Type", "application/json")
            .bodyValue(requestBody)
            .retrieve()
            .bodyToMono(String.class)
            .onErrorMap(e -> new RuntimeException("Failed to start workflow", e));
    }*/
}
