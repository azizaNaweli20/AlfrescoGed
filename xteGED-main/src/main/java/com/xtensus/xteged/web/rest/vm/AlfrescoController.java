package com.xtensus.xteged.web.rest.vm;

import com.xtensus.xteged.service.AlfrescoService;
import com.xtensus.xteged.service.CmisService;
import com.xtensus.xteged.service.DocumentDetails;
import com.xtensus.xteged.service.ldap.Person;
import com.xtensus.xteged.service.person.PeopleListResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.chemistry.opencmis.client.api.Folder;
import reactor.core.publisher.Mono;

@RestController

@RequestMapping("/api/ged-controller")
public class AlfrescoController {
    private final AlfrescoService alfrescoService;
    @Autowired
    private CmisService cmisService;


    @Value("${alfresco.repository.url}")
    private String alfrescoUrl;

    @Value("${alfresco.repository.user}")
    private String alfrescoUser;

    @Value("${alfresco.repository.pass}")
    private String alfrescoPassword;

    public AlfrescoController(AlfrescoService alfrescoService) {
        this.alfrescoService = alfrescoService;
    }
    @PutMapping("/uu/share")
    public Mono<String> shareDocument(
        @RequestParam String nodeId,
        @RequestParam String userId,
        @RequestParam String role) {
        return alfrescoService.shareDocument(nodeId, userId, role);
    }


    @GetMapping("/mm/getPeopleList")
        public Mono<ResponseEntity<PeopleListResponse>> getPeopleList(
            @RequestParam(defaultValue = "0") int skipCount,
            @RequestParam(defaultValue = "10") int maxItems,
            @RequestParam(required = false) String orderBy,
            @RequestParam(required = false) String[] include,
            @RequestParam(required = false) String[] fields) {
            return alfrescoService.getPeopleList(skipCount, maxItems, orderBy, include, fields)
                .map(response -> ResponseEntity.ok().body(response))
                .onErrorResume(e -> Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()));
        }


    @DeleteMapping("/nodes/{nodeId}")
    public Mono<String> deleteNode(@PathVariable String nodeId,
                                   @RequestParam(defaultValue = "false") boolean permanent) {
        return alfrescoService.deleteNode(nodeId, permanent);
    }

    @PostMapping("/nodes/{nodeId}/unlock")
    public Mono<ResponseEntity<String>> unlockNode(@PathVariable("nodeId") String nodeId) {
        return alfrescoService.unlockNode(nodeId)
            .map(response -> ResponseEntity.ok().body(response))
            .onErrorResume(e -> Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage())));
    }



    @PostMapping("/{nodeId}/lock")
    public Mono<ResponseEntity<String>> lockNode(
        @PathVariable String nodeId,
        @RequestParam(value = "timeToExpire", defaultValue = "0") int timeToExpire,
        @RequestParam(value = "type", defaultValue = "ALLOW_OWNER_CHANGES") String type,
        @RequestParam(value = "lifetime", defaultValue = "PERSISTENT") String lifetime) {

        return alfrescoService.lockNode(nodeId, timeToExpire, type, lifetime)
            .map(response -> ResponseEntity.ok().body(response))
            .onErrorResume(e -> Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage())));
    }
    @GetMapping("/oo/search")
    public Mono<ResponseEntity<String>> searchNodes(
        @RequestParam String term,
        @RequestParam(required = false) String rootNodeId,
        @RequestParam(required = false) Integer skipCount,
        @RequestParam(required = false) Integer maxItems,
        @RequestParam(required = false) String nodeType,
        @RequestParam(required = false) List<String> include,
        @RequestParam(required = false) List<String> orderBy,
        @RequestParam(required = false) List<String> fields) {

        return alfrescoService.searchNodes(term, rootNodeId, skipCount, maxItems, nodeType, include, orderBy, fields)
            .map(response -> ResponseEntity.ok().body(response))
            .onErrorResume(e -> Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage())));
    }

    @GetMapping("/nodes/deleted")
    public Mono<String> getDeletedNodes(
        @RequestParam(required = false) Integer maxItems,
        @RequestParam(required = false) Integer skipCount,
        @RequestParam(required = false) String[] include) {
        return alfrescoService.getDeletedNodes(maxItems, skipCount, include);
    }

    @GetMapping("/search/people")
    public Mono<String> searchPeople(
        @RequestParam String term, // Terme de recherche pour filtrer les personnes
        @RequestParam(required = false) Integer maxItems,
        @RequestParam(required = false) Integer skipCount,
        @RequestParam(required = false) String[] orderBy) {

        return alfrescoService.searchPeople(term, maxItems, skipCount, orderBy);
    }


    @GetMapping("/yyy/{nodeId}")
    public Mono<ResponseEntity<String>> getNode(
        @PathVariable String nodeId,
        @RequestParam(value = "include", required = false) String[] include,
        @RequestParam(value = "fields", required = false) String[] fields) {

        return alfrescoService.getNode(nodeId, include, fields)
            .map(response -> ResponseEntity.ok().body(response))
            .onErrorResume(e -> Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage())));
    }
    @PostMapping("/nodes/{nodeId}/copy")
    public Mono<String> copyNode(@PathVariable String nodeId,
                                 @RequestBody Map<String, String> requestBody) {
        String targetParentId = requestBody.get("targetParentId");
        String newName = requestBody.get("name");
        return alfrescoService.copyNode(nodeId, targetParentId, newName);
    }
    @PutMapping("/updete/{nodeId}")
    public Mono<ResponseEntity<String>> updateNode(@PathVariable String nodeId, @RequestBody String jsonRequestBody) {
        return alfrescoService.updateNode(nodeId, jsonRequestBody);
    }
    @PostMapping("/alfresco/upload")
    public ResponseEntity<String> uploadFileToAlfresco(@RequestParam("file") MultipartFile file,
                                                       @RequestParam("path") String path,
                                                       @RequestHeader("Authorization") String authToken) {
        try {
            // Initialize the Alfresco session
            alfrescoService.initializeSession(alfrescoUser, alfrescoPassword, alfrescoUrl);

            String filename = file.getOriginalFilename();
            return alfrescoService.uploadFileWithAutoRename(file, path, filename, "", 0, authToken);
        } catch (IOException e) {
            return new ResponseEntity<>("Erreur lors du téléchargement du fichier: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @GetMapping(value = "/getDocumentDetailsByAlfrescoId/{alfrescoId}")
    public ResponseEntity<DocumentDetails> getDocumentDetailsByAlfrescoId(@PathVariable("alfrescoId") String alfrescoId) {
        DocumentDetails documentDetails = cmisService.getDocumentDetails(alfrescoId);

        if (documentDetails != null) {
            return ResponseEntity.ok(documentDetails);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
   @GetMapping("/bb/{nodeId}")
    public ResponseEntity<String> getNodeDetails(@PathVariable String nodeId) {
        return cmisService.getNodeDetails(nodeId);
    }

    @PostMapping("/persons")
    public Mono<ResponseEntity<Person>> createPerson(@RequestBody Person person) {
        return alfrescoService.createPerson(person)
            .map(createdPerson -> ResponseEntity.status(201).body(createdPerson))
            .onErrorResume(e -> Mono.just(ResponseEntity.status(500).build()));
    }



    }











   /* private final AlfrescoService alfrescoService;

    @Autowired
    public AlfrescoController(AlfrescoService alfrescoService) {
        this.alfrescoService = alfrescoService;
    }

    @GetMapping("/{nodeId}")
    public AlfrescoNodeResponsee getNodeDetails(@PathVariable String nodeId) {
        return alfrescoService.getNodeDetails(nodeId);
    }*/

  /*  @Autowired
    private AlfrescoService alfrescoService;



    @PostMapping("/nodes/{nodeId}/share")
    public ResponseEntity<String> shareDocumentWithUser(@PathVariable String nodeId,
                                                        @RequestParam String authorityId,
                                                        @RequestParam String permissionRole) {
        String response = alfrescoService.shareDocumentWithUser(nodeId, authorityId, permissionRole);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping(value = "/upload-document", consumes = "multipart/form-data")
    public ResponseEntity<String> uploadDocument(
        @RequestParam("pathName") String pathName,
        @RequestParam("fileName") String fileName,
        @RequestPart("file") MultipartFile file
    ) {
        try {
            String extension = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf(".") + 1);
            fileName += "." + extension;

            String alfrescoId = alfrescoService.uploadDocument(file.getBytes(), pathName, fileName, extension);
            return new ResponseEntity<>(alfrescoId, HttpStatus.OK);
        } catch (IOException e) {
            e.printStackTrace();
            return new ResponseEntity<>("Document n'a pas été uploadé", HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Document n'a pas été uploadé", HttpStatus.BAD_REQUEST);
        }
    }
}*/
