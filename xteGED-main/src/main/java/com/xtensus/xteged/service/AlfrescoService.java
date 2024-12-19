package com.xtensus.xteged.service;

import com.xtensus.xteged.service.impl.CmisServiceImpl;
import com.xtensus.xteged.service.person.PeopleListResponse;
import com.xtensus.xteged.service.person.PersonEntry;
import org.apache.chemistry.opencmis.client.api.*;
import org.apache.chemistry.opencmis.client.runtime.SessionFactoryImpl;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.definitions.PropertyDefinition;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinition;
import org.apache.chemistry.opencmis.commons.enums.BindingType;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.exceptions.CmisBaseException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ContentStreamImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.*;

@Service
public class AlfrescoService {
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

    public AlfrescoService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("http://localhost:8080/alfresco/api/-default-/public/alfresco/versions/1").build(); // Assurez-vous de définir l'URL de base appropriée
    }

    private static final HashMap<String, String> mimeTypeMapping = new HashMap<>();
    private final Logger log = LoggerFactory.getLogger(CmisServiceImpl.class);

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
////////////////////////////////////////////////////////////////////

    public Mono<Object> getAllDocuments(String nodeId, String include) {

        String url = String.format("/nodes/%s/children", nodeId);

        return webClient.get()
            .uri(uriBuilder -> uriBuilder
                .path(url)
                .queryParam("include", include)
                .build())
            .header(HttpHeaders.AUTHORIZATION, "Basic " + Base64.getEncoder().encodeToString((alfrescoUser + ":" + alfrescoPass).getBytes()))
            .retrieve()
            .onStatus(HttpStatus::isError, response ->
                response.bodyToMono(String.class)
                    .flatMap(errorBody -> Mono.error(new RuntimeException("Failed to get nodes: " + errorBody)))
            )
            .bodyToMono(Object.class); // Remplacez Object par un modèle spécifique si nécessaire
    }



    //////////////////deleteNode/////////////////////
    public Mono<String> deleteNode(String nodeId, boolean permanent) {
        String url = String.format("%s/nodes/%s?permanent=%b", alfrescoUrl, nodeId, permanent);

        return webClient.delete()
            .uri(url)
            .header(HttpHeaders.AUTHORIZATION, "Basic " + Base64.getEncoder().encodeToString((alfrescoUser + ":" + alfrescoPass).getBytes()))
            .retrieve()
            .onStatus(HttpStatus::isError, response -> Mono.error(new RuntimeException("Failed to delete node")))
            .bodyToMono(Void.class)
            .then(Mono.just("Node deleted successfully"));
    }

    ////////////////////////////////
    public Mono<byte[]> downloadFileContent(String nodeId) {
        String url = String.format("%s/nodes/%s/content", alfrescoUrl, nodeId);

        return webClient.get()
            .uri(url)
            .header(HttpHeaders.AUTHORIZATION, "Basic " + Base64.getEncoder().encodeToString((alfrescoUser + ":" + alfrescoPass).getBytes()))
            .retrieve()
            .onStatus(HttpStatus::isError, response ->
                response.bodyToMono(String.class)
                    .flatMap(errorBody -> {
                        // Log de l'erreur pour plus de détails
                        System.err.println("Erreur lors du téléchargement : " + errorBody);
                        return Mono.error(new RuntimeException("Failed to download file content: " + errorBody));
                    })
            )
            .bodyToMono(byte[].class);
    }

    //////////////////////////////////
public Mono<String> createPerson(PersonneRequest personRequest) {
    String url = String.format("%s/people", alfrescoUrl);

    return webClient.post()
        .uri(url)
        .header(HttpHeaders.AUTHORIZATION, "Basic " + Base64.getEncoder().encodeToString((alfrescoUser + ":" + alfrescoPass).getBytes()))
        .contentType(MediaType.APPLICATION_JSON)
        .body(BodyInserters.fromValue(personRequest))
        .retrieve()
        .onStatus(HttpStatus::isError, response -> response.bodyToMono(String.class)
            .flatMap(errorBody -> Mono.error(new RuntimeException("Failed to create person: " + errorBody))))
        .bodyToMono(String.class)
        .map(responseBody -> "Person created successfully: " + responseBody);
}




    // Méthode pour obtenir les détails d'un nœud
    public Mono<String> getNode(String nodeId, String[] include, String[] fields) {
        // Construire l'URL en ajoutant les paramètres query
        String url = String.format("%s/nodes/%s", alfrescoUrl, nodeId);
        StringBuilder uriBuilder = new StringBuilder(url);

        // Ajouter les paramètres include
        if (include != null && include.length > 0) {
            uriBuilder.append("?include=").append(String.join(",", include));
        }

        // Ajouter les paramètres fields
        if (fields != null && fields.length > 0) {
            if (uriBuilder.indexOf("?") == -1) {
                uriBuilder.append("?fields=").append(String.join(",", fields));
            } else {
                uriBuilder.append("&fields=").append(String.join(",", fields));
            }
        }

        return webClient.get()
            .uri(uriBuilder.toString())
            .header(HttpHeaders.AUTHORIZATION, "Basic " + Base64.getEncoder().encodeToString((alfrescoUser + ":" + alfrescoPass).getBytes()))
            .retrieve()
            .onStatus(HttpStatus::isError, response -> {
                log.error("Failed to retrieve node details. Status code: {}", response.statusCode());
                return Mono.error(new RuntimeException("Failed to retrieve node details"));
            })
            .bodyToMono(String.class);
    }

    //////////////////////////////////
    public Mono<String> getDeletedNodes(Integer maxItems, Integer skipCount, String[] include) {
        // Construire l'URL en ajoutant les paramètres query
        String url = String.format("%s/deleted-nodes", alfrescoUrl);
        StringBuilder uriBuilder = new StringBuilder(url);

        // Ajouter les paramètres maxItems
        if (maxItems != null) {
            uriBuilder.append("?maxItems=").append(maxItems);
        }

        // Ajouter les paramètres skipCount
        if (skipCount != null) {
            if (uriBuilder.indexOf("?") == -1) {
                uriBuilder.append("?skipCount=").append(skipCount);
            } else {
                uriBuilder.append("&skipCount=").append(skipCount);
            }
        }

        // Ajouter les paramètres include
        if (include != null && include.length > 0) {
            if (uriBuilder.indexOf("?") == -1) {
                uriBuilder.append("?include=").append(String.join(",", include));
            } else {
                uriBuilder.append("&include=").append(String.join(",", include));
            }
        }

        return webClient.get()
            .uri(uriBuilder.toString())
            .header(HttpHeaders.AUTHORIZATION, "Basic " + Base64.getEncoder().encodeToString((alfrescoUser + ":" + alfrescoPass).getBytes()))
            .retrieve()
            .onStatus(HttpStatus::isError, response -> {
                return response.bodyToMono(String.class)
                    .flatMap(body -> Mono.error(new RuntimeException("Failed to retrieve deleted nodes: " + body)));
            })
            .bodyToMono(String.class);
    }


//////////////////////////////////////////searchPeople//////////////////////////////////////

    public Mono<String> searchPeople(String term, Integer maxItems, Integer skipCount, String[] orderBy) {
        // Construire l'URL en ajoutant les paramètres query
        String url = String.format("%s/queries/people?term=%s", alfrescoUrl, term);
        StringBuilder uriBuilder = new StringBuilder(url);

        // Ajouter les paramètres maxItems
        if (maxItems != null) {
            uriBuilder.append("&maxItems=").append(maxItems);
        }

        // Ajouter les paramètres skipCount
        if (skipCount != null) {
            uriBuilder.append("&skipCount=").append(skipCount);
        }

        // Ajouter les paramètres orderBy
        if (orderBy != null && orderBy.length > 0) {
            uriBuilder.append("&orderBy=").append(String.join(",", orderBy));
        }

        return webClient.get()
            .uri(uriBuilder.toString())
            .header(HttpHeaders.AUTHORIZATION, "Basic " + Base64.getEncoder().encodeToString((alfrescoUser + ":" + alfrescoPass).getBytes()))
            .retrieve()
            .onStatus(HttpStatus::isError, response -> {
                log.error("Failed to retrieve people details. Status code: {}", response.statusCode());
                return Mono.error(new RuntimeException("Failed to retrieve people details"));
            })
            .bodyToMono(String.class);
    }


    //////////////////////////
    public Mono<String> shareDocument(String nodeId, String authorityId, String permission) {
        String url = String.format("%s/nodes/%s/permissions", alfrescoUrl, nodeId);

        // Corps de la requête pour mettre à jour les permissions
        String requestBody = String.format("{\"entries\":[{\"authority\":\"%s\",\"permissions\":[\"%s\"]}]}", authorityId, permission);

        return webClient.put()
            .uri(url)
            .header("Authorization", "Basic " + Base64.getEncoder().encodeToString((alfrescoUser + ":" + alfrescoPass).getBytes()))
            .header("Content-Type", "application/json")
            .bodyValue(requestBody)
            .retrieve()
            .onStatus(HttpStatus::isError, response -> {
                return Mono.error(new RuntimeException("Failed to share document: " + response.statusCode()));
            })
            .bodyToMono(String.class);
    }


    ////////////////////////////////////////////////
    public Mono<PeopleListResponse> getPeopleList(int skipCount, int maxItems, String orderBy, String[] include, String[] fields) {
        String url = String.format("%s/people", alfrescoUrl);
        StringBuilder uriBuilder = new StringBuilder(url);

        uriBuilder.append("?skipCount=").append(skipCount);
        uriBuilder.append("&maxItems=").append(maxItems);

        if (orderBy != null && !orderBy.trim().isEmpty()) {
            uriBuilder.append("&orderBy=").append(orderBy);
        }

        if (include != null && include.length > 0) {
            uriBuilder.append("&include=").append(String.join(",", include));
        }

        if (fields != null && fields.length > 0) {
            uriBuilder.append("&fields=").append(String.join(",", fields));
        }

        String finalUri = uriBuilder.toString();
        log.info("Requesting people list from URL: {}", finalUri);

        return webClient.get()
            .uri(finalUri)
            .header(HttpHeaders.AUTHORIZATION, "Basic " + Base64.getEncoder().encodeToString((alfrescoUser + ":" + alfrescoPass).getBytes()))
            .retrieve()
            .onStatus(HttpStatus::is4xxClientError, response -> {
                String errorMessage = String.format("Client error while retrieving people list. Status code: %d", response.statusCode().value());
                log.error(errorMessage);
                return Mono.error(new RuntimeException(errorMessage));
            })
            .onStatus(HttpStatus::is5xxServerError, response -> {
                String errorMessage = String.format("Server error while retrieving people list. Status code: %d", response.statusCode().value());
                log.error(errorMessage);
                return Mono.error(new RuntimeException(errorMessage));
            })
            .bodyToMono(PeopleListResponse.class)
            .doOnError(e -> log.error("Error occurred while retrieving people list", e));
    }


    public Mono<ResponseEntity<Object>> getNodeDetails(String nodeId, String include, String fields) {
        String url = String.format("%s/nodes/%s", alfrescoUrl, nodeId);

        return webClient.get()
            .uri(uriBuilder -> uriBuilder

                .queryParamIfPresent("include", Optional.ofNullable(include))
                .queryParamIfPresent("fields", Optional.ofNullable(fields))
                .build(nodeId))
            .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
            .header(HttpHeaders.AUTHORIZATION, "Basic " + Base64.getEncoder().encodeToString((alfrescoUser + ":" + alfrescoPass).getBytes()))
            .retrieve()
            .toEntity(Object.class) // Remplacez par votre classe de réponse
            .doOnSuccess(response -> {
                if (response.getStatusCode().is2xxSuccessful()) {
                    log.info("Received node response: {}", response.getBody());
                }
            })
            .doOnError(error -> log.error("Error fetching node details: {}", error.getMessage()))
            .onErrorResume(e -> {
                log.error("An error occurred while fetching node details: {}", e.getMessage());
                return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Une erreur s'est produite"));
            });
    }









    public Mono<PersonneResponse> getPersonById(String personId) {
        String url = String.format("%s/people/%s", alfrescoUrl, personId);

        return webClient.get()
            .uri(url)
            .header(HttpHeaders.AUTHORIZATION, "Basic " + Base64.getEncoder().encodeToString((alfrescoUser + ":" + alfrescoPass).getBytes()))
            .retrieve()
            .onStatus(HttpStatus::isError, response -> {
                log.error("Error fetching person: {}", response.statusCode());
                return Mono.error(new RuntimeException("Failed to fetch person"));
            })
            .bodyToMono(PersonneResponse.class)
            .doOnNext(personResponse -> {
                log.info("Received person response: {}", personResponse);
            });
    }
/////////////////////////////////put person///////////////////////

    public Mono<PersonneResponse> updatePerson(String personId, PersonneBodyUpdate bodyUpdate) {
        String url = String.format("%s/people/%s", alfrescoUrl, personId);

        return webClient.put()
            .uri(url)
            .header(HttpHeaders.AUTHORIZATION, "Basic " + Base64.getEncoder().encodeToString((alfrescoUser + ":" + alfrescoPass).getBytes()))
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(bodyUpdate)
            .retrieve()
            .onStatus(HttpStatus::isError, response -> {
                return response.bodyToMono(String.class)
                    .flatMap(errorBody -> Mono.error(new RuntimeException("Error updating person: " + errorBody)));
            })
            .bodyToMono(PersonneResponse.class)
            .doOnNext(personResponse -> {
                // Log ou traitement après une mise à jour réussie
                System.out.println("Successfully updated person: " + personResponse.getEntry());
            });
    }




















    //////////////////searchNodes//////////////////////////////////


    public Mono<String> searchNodes(String term, String rootNodeId, Integer skipCount, Integer maxItems, String nodeType, List<String> include, List<String> orderBy, List<String> fields) {
        return webClient.get()
            .uri(uriBuilder -> uriBuilder
                .path("/queries/nodes")
                .queryParam("term", term)
                .queryParamIfPresent("rootNodeId", Optional.ofNullable(rootNodeId))
                .queryParamIfPresent("skipCount", Optional.ofNullable(skipCount))
                .queryParamIfPresent("maxItems", Optional.ofNullable(maxItems))
                .queryParamIfPresent("nodeType", Optional.ofNullable(nodeType))
                .queryParamIfPresent("include", Optional.ofNullable(include).flatMap(l -> Optional.of(String.join(",", l))))
                .queryParamIfPresent("orderBy", Optional.ofNullable(orderBy).flatMap(l -> Optional.of(String.join(",", l))))
                .queryParamIfPresent("fields", Optional.ofNullable(fields).flatMap(l -> Optional.of(String.join(",", l))))
                .build())
            .header(HttpHeaders.AUTHORIZATION, "Basic " + Base64.getEncoder().encodeToString((alfrescoUser + ":" + alfrescoPass).getBytes()))
            .retrieve()
            .onStatus(HttpStatus::isError, response -> Mono.error(new RuntimeException("Failed to search nodes")))
            .bodyToMono(String.class);
    }
    public Mono<String> unlockNode(String nodeId) {
        String url = String.format("%s/nodes/%s/unlock", alfrescoUrl, nodeId);

        return webClient.post()
            .uri(url)
            .header(HttpHeaders.AUTHORIZATION, "Basic " + Base64.getEncoder().encodeToString((alfrescoUser + ":" + alfrescoPass).getBytes()))
            .retrieve()
            .onStatus(HttpStatus::is4xxClientError, clientResponse ->
                clientResponse.bodyToMono(String.class)
                    .flatMap(errorBody -> Mono.error(new RuntimeException("Client error: " + clientResponse.statusCode() + " - " + errorBody)))
            )
            .onStatus(HttpStatus::is5xxServerError, serverResponse ->
                serverResponse.bodyToMono(String.class)
                    .flatMap(errorBody -> Mono.error(new RuntimeException("Server error: " + serverResponse.statusCode() + " - " + errorBody)))
            )
            .bodyToMono(String.class)
            .defaultIfEmpty("Node unlocked successfully");
    }


    public Mono<String> lockNode(String nodeId, int timeToExpire, String type, String lifetime) {
        String url = String.format("%s/nodes/%s/lock", alfrescoUrl, nodeId);

        // Crée le corps de la requête
        Map<String, Object> body = Map.of(
            "timeToExpire", timeToExpire,
            "type", type,
            "lifetime", lifetime
        );

        return webClient.post()
            .uri(url)
            .header(HttpHeaders.AUTHORIZATION, "Basic " + Base64.getEncoder().encodeToString((alfrescoUser + ":" + alfrescoPass).getBytes()))
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(body)
            .retrieve()
            .onStatus(HttpStatus::isError, response -> Mono.error(new RuntimeException("Failed to lock node")))
            .bodyToMono(String.class);
    }









    ///////////copier un nœud dans Alfresco///////////////////////
    public Mono<String> copyNode(String nodeId, String targetParentId, String newName) {
        String url = String.format("%s/nodes/%s/copy", alfrescoUrl, nodeId);
        Map<String, String> body = Map.of(
            "targetParentId", targetParentId,
            "name", newName != null ? newName : ""
        );

        return webClient.post()
            .uri(url)
            .header(HttpHeaders.AUTHORIZATION, "Basic " + Base64.getEncoder().encodeToString((alfrescoUser + ":" + alfrescoPass).getBytes()))
            .bodyValue(body)
            .retrieve()
            .onStatus(HttpStatus::isError, response -> Mono.error(new RuntimeException("Failed to copy node")))
            .bodyToMono(String.class);
    }

              ////////////////////updateNode//////////////////////////////
    public Mono<ResponseEntity<String>> updateNode(String nodeId, String jsonRequestBody) {
        String url = String.format("%s/nodes/%s", alfrescoUrl, nodeId);

        return webClient.put()
            .uri(url)
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
            .header(HttpHeaders.AUTHORIZATION, "Basic " + Base64.getEncoder().encodeToString((alfrescoUser + ":" + alfrescoPass).getBytes()))
            .body(Mono.just(jsonRequestBody), String.class)
            .retrieve()
            .toEntity(String.class)
            .onErrorResume(WebClientResponseException.class, ex -> Mono.error(new RuntimeException("Failed to update node: " + ex.getResponseBodyAsString())));
    }



    public Mono<String> getAllDocuments(String folderId, String user, String pass) {
        return webClient.get()
            .uri("/nodes/{folderId}/children", folderId)
            .header(HttpHeaders.AUTHORIZATION, "Basic " + Base64.getEncoder().encodeToString((alfrescoUser + ":" + alfrescoPass).getBytes()))
            .retrieve()
            .bodyToMono(String.class)
            .onErrorResume(WebClientResponseException.class, ex -> {
                return Mono.error(new RuntimeException("Failed to retrieve documents", ex));
            });
    }













    // Méthode pour partager un document avec un utilisateur
   /* public Mono<Void> shareDocument(String nodeId, String user, String role) {
        String url = alfrescoRepoUrl.replace("/public/cmis/versions/1.1/atom", "") + "/public/alfresco/versions/1/nodes/" + nodeId ;

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("authorityId", user);
        requestBody.put("name", role);
        requestBody.put("accessStatus", "ALLOWED");

        return webClient.post()
            .uri(url)
            .headers(headers -> headers.setBasicAuth(alfrescoUser, alfrescoPass))
            .bodyValue(requestBody)
            .retrieve()
            .bodyToMono(Void.class)
            .doOnSuccess(aVoid -> log.info("Document shared successfully with user {} and role {}", user, role))
            .doOnError(error -> log.error("Error sharing document: {}", error.getMessage()));
    }*/


    public Document updateDocumentMetadata(String documentId, Map<String, Object> properties) {
        try {
            // Retrieve the document
            Document document = (Document) session.getObject(documentId);

            // Retrieve the type definition of the document
            TypeDefinition typeDefinition = session.getTypeDefinition(document.getBaseTypeId().value());
            Map<String, PropertyDefinition<?>> propertyDefinitions = typeDefinition.getPropertyDefinitions();

            // Filter valid properties
            Map<String, Object> validProperties = new HashMap<>();
            for (Map.Entry<String, Object> entry : properties.entrySet()) {
                if (propertyDefinitions.containsKey(entry.getKey())) {
                    validProperties.put(entry.getKey(), entry.getValue());
                } else {
                    System.out.println("Property '" + entry.getKey() + "' is not valid for this type or one of the secondary types.");
                }
            }

            // Update the properties if there are any valid properties
            if (!validProperties.isEmpty()) {
                document.updateProperties(validProperties);
            }

            return document;
        } catch (CmisObjectNotFoundException e) {
            System.err.println("Document not found: " + documentId);
            return null;
        } catch (CmisBaseException e) {
            System.err.println("Error updating document metadata: " + e.getMessage());
            return null;
        } catch (Exception e) {
            System.err.println("Unexpected error updating document metadata: " + e.getMessage());
            return null;
        }

    }




        /*public String getNodeDetails(String nodeId) {
        try {
            Document document = (Document) session.getObject(nodeId);
            return String.format("Node Name: %s, Type: %s, Created: %s, Modified: %s",
                document.getName(),
                document.getBaseTypeId(),
                document.getCreationDate(),
                document.getLastModificationDate());
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des détails du nœud: " + e.getMessage(), e);
            return "Erreur lors de la récupération des détails du nœud: " + e.getMessage();
        }
    }*/

    // Méthode pour obtenir le dossier racine
    public Folder getRootFolder() {
        try {
            return session.getRootFolder();
        } catch (Exception e) {
            log.error("Erreur lors de la récupération du dossier racine: " + e.getMessage(), e);
            return null;
        }
    }



    public ResponseEntity<String> uploadFileWithAutoRename(MultipartFile file, String path, String filename, String filenameAux, int index, String authToken) throws IOException {

        if (this.session == null) {
            throw new IllegalStateException("La session CMIS n'est pas initialisée.");
        }

        if (authToken == null || authToken.isEmpty()) {
            return new ResponseEntity<>("Problème d'authentification Alfresco !", HttpStatus.BAD_REQUEST);
        }

        // Créer l'en-tête d'authentification
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + authToken);

        Folder folder = (Folder) session.getObjectByPath(path);
        InputStream stream = file.getInputStream();
        ContentStream contentStream = new ContentStreamImpl(file.getOriginalFilename(), BigInteger.valueOf(file.getSize()), file.getContentType(), stream);

        Map<String, Object> properties = new HashMap<>();
        properties.put(PropertyIds.OBJECT_TYPE_ID, "cmis:document");
        properties.put(PropertyIds.NAME, filename);

        String noderefFile = null;

        try {
            String filenamePath = folder.getPath() + "/" + filename;
            boolean fileExists = session.existsPath(filenamePath);

            if (!fileExists) {
                Document document = folder.createDocument(properties, contentStream, VersioningState.MAJOR);
                noderefFile = document.getId().split(";")[0];
            } else {
                if (filenameAux.isEmpty()) {
                    String baseName = filename.substring(0, filename.lastIndexOf("."));
                    int count = index + 1;
                    String newFilename = baseName + "-" + count + filename.substring(filename.lastIndexOf("."));
                    return uploadFileWithAutoRename(file, path, newFilename, filename, count, authToken);
                } else {
                    String baseName = filenameAux.substring(0, filenameAux.lastIndexOf("."));
                    int count = index + 1;
                    String newFilename = baseName + "-" + count + filename.substring(filename.lastIndexOf("."));
                    return uploadFileWithAutoRename(file, path, newFilename, filenameAux, count, authToken);
                }
            }
        } catch (Exception e) {
            return new ResponseEntity<>("Erreur lors de la création du document dans Alfresco: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity<>(noderefFile.substring(noderefFile.lastIndexOf("/") + 1), HttpStatus.OK);
    }


}

 /*   //////////////////////getNodeDetails/////////////////////////

    public Node getNodeDetails(String nodeId) {
        if (this.session == null) {
            // Initialize the CMIS session here
            this.session = initializeCMISSession();
        }

        if (this.session != null) {
            try {
                // Get the node by ID
                Document document = (Document) session.getObject(session.createObjectId(nodeId));

                // Get node properties
                Property<String> nameProperty = document.getProperty(PropertyIds.NAME);
                Property<String> descriptionProperty = document.getProperty(PropertyIds.DESCRIPTION);
                Property<String> mimeTypeProperty = document.getProperty(PropertyIds.CONTENT_STREAM_MIME_TYPE);
                Property<Long> fileSizeProperty = document.getProperty(PropertyIds.CONTENT_STREAM_LENGTH);

                // Create a custom Node object to hold the node details
                Node nodeDetails = new Node();
                nodeDetails.setId(nodeId);
                nodeDetails.setName(nameProperty.getValue());
                nodeDetails.setDescription(descriptionProperty.getValue());
                nodeDetails.setMimeType(mimeTypeProperty.getValue());
                nodeDetails.setFileSize(fileSizeProperty.getValue());

                return nodeDetails;
            } catch (CmisObjectNotFoundException e) {
                // Handle node not found exception
                return null;
            }
        } else {
            throw new IllegalStateException("La session CMIS n'est pas initialisée.");
        }
    }

    private Session initializeCMISSession() {
        // Initialize the CMIS session here
        // For example:
        SessionFactory factory = SessionFactoryImpl.newInstance();
        Repository repository = factory.getRepositories().get(0);
        Session session = repository.createSession();
        return session;
    }
*/


















/*
//////////////////////////getNodePermissions/////////////////////////
    public ResponseEntity<List<Ace>> getNodePermissions(String nodeId) {
        if (session == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        try {
            CmisObject object = session.getObject(nodeId);
            if (object instanceof Document) {
                Acl acl = object.getAcl();
                return new ResponseEntity<>(acl.getAces(), HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
   */
//////////////////////getNodeDetails/////////////////////////
   /* public ResponseEntity<CmisObject> getNodeDetails(String nodeId) {
        if (session == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        try {
            CmisObject object = session.getObject(nodeId);
            return new ResponseEntity<>(object, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
*/

    /*
    private final RestTemplate restTemplate;
    private final String alfrescoApiUrl;
    private final String alfrescoApiUser;
    private final String alfrescoApiPassword;
    public AlfrescoService(RestTemplate restTemplate,
                           @Value("${alfresco.repository.url}") String alfrescoApiUrl,
                           @Value("${alfresco.repository.user}") String alfrescoApiUser,
                           @Value("${alfresco.repository.pass}") String alfrescoApiPassword) {
        this.restTemplate = restTemplate;
        this.alfrescoApiUrl = alfrescoApiUrl;
        this.alfrescoApiUser = alfrescoApiUser;
        this.alfrescoApiPassword = alfrescoApiPassword;}


    public AlfrescoNodeResponsee getNodeDetails(String nodeId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(alfrescoApiUser, alfrescoApiPassword);
        System.out.println("okkkkkkkkkkkkkkkk111");
        String uri = UriComponentsBuilder.fromHttpUrl(alfrescoApiUrl)
            .path("/nodes/{nodeId}")
            .queryParam("include", "properties,path,permissions")
            .buildAndExpand(nodeId)
            .toUriString();
        System.out.println("okkkkkkkkkkkkkkkk2222");
        try {
            ResponseEntity<AlfrescoNodeResponsee> responseEntity = restTemplate.exchange(
                uri,
                HttpMethod.GET,
                null,
                AlfrescoNodeResponsee.class);
            System.out.println(" ok33333333333333");
            return responseEntity.getBody();
        } catch (HttpClientErrorException.Unauthorized ex) {
            // Gestion spécifique de l'erreur 401 Non-Autorisé
            // Par exemple, journalisation de l'erreur ou lancer une exception personnalisée
            throw new RuntimeException("Erreur d'authentification Alfresco", ex);
        }
    }
}




*/
