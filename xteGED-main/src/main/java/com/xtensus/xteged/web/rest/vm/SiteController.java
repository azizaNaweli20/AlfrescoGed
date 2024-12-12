package com.xtensus.xteged.web.rest.vm;

import com.xtensus.xteged.service.SiteMembership;
import com.xtensus.xteged.service.SiteResponse;
import com.xtensus.xteged.service.SiteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;


@RestController
@RequestMapping("/api/ged-controller")
public class SiteController {
    @Autowired
    private SiteService siteService;



    @GetMapping("/alfresco/sites")
    public Mono<ResponseEntity<String>> getSites(
        @RequestHeader("Authorization") String authorization,
        @RequestParam(value = "visibility", required = false) String visibility,
        @RequestParam(value = "orderBy", required = false) String orderBy,
        @RequestParam(value = "skipCount", defaultValue = "0") int skipCount,
        @RequestParam(value = "maxItems", defaultValue = "100") int maxItems) {

        String accessToken = authorization.replace("Bearer ", "");
        return siteService.getSites(visibility, orderBy, skipCount, maxItems)
            .map(ResponseEntity::ok)
            .onErrorResume(e -> Mono.just(ResponseEntity.status(500).body(e.getMessage())));
    }


    @PostMapping("/create-site")
    public Mono<ResponseEntity<String>> createSite(
        @RequestParam String title,
        @RequestParam String visibility,
        @RequestParam(required = false) String description,
        @RequestParam(defaultValue = "false") boolean skipConfiguration,
        @RequestParam(defaultValue = "false") boolean skipAddToFavorites) {

        return siteService.createSite(title, visibility, description, skipConfiguration, skipAddToFavorites)
            .map(response -> ResponseEntity.status(HttpStatus.CREATED).body(response))
            .onErrorResume(e -> Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage())));
    }

    @DeleteMapping("/delete-site/{siteId}")
    public Mono<ResponseEntity<Object>> deleteSite(
        @PathVariable String siteId,
        @RequestParam(defaultValue = "false") boolean permanent) {

        return siteService.deleteSite(siteId, permanent)
            .then(Mono.just(ResponseEntity.noContent().build()))
            .onErrorResume(e -> {
                // Log de l'erreur et retour de la réponse avec le message d'erreur
                e.printStackTrace();
                return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", e.getMessage())));
            });
    }

    @GetMapping("/get-site/{siteId}")
    public Mono<ResponseEntity<SiteResponse>> getSiteInfo(
        @PathVariable String siteId,
        @RequestParam(required = false) List<String> relations,
        @RequestParam(required = false) List<String> fields) {

        return siteService.getSiteInfo(siteId, relations, fields)
            .map(ResponseEntity::ok)
            .onErrorResume(e -> Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(null)));
    }

    @PutMapping("/update-site/{siteId}")
    public Mono<ResponseEntity<String>> updateSite(
        @PathVariable String siteId,
        @RequestBody SiteUpdateRequest siteUpdateRequest) {

        return siteService.updateSite(siteId, siteUpdateRequest)
            .map(response -> ResponseEntity.ok("Site modifié avec succès !")) // Retourne un message de succès
            .onErrorResume(e -> Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Échec de la mise à jour du site.")));
    }


    @PostMapping("/sites/{siteId}/members")
    public Mono<ResponseEntity<String>> addMember(
        @PathVariable String siteId,
        @RequestParam String id,
        @RequestParam String role) {

        return siteService.addMemberToSite(siteId, id, role)
            .map(response -> ResponseEntity.status(201).body(response))
            .onErrorReturn(ResponseEntity.badRequest().body("Erreur lors de l'ajout du membre."));

}

    @DeleteMapping("/sites/{siteId}/members/{personId}")
    public Mono<ResponseEntity<String>> deleteSiteMember(
        @PathVariable String siteId,
        @PathVariable String personId) {

        return siteService.deleteSiteMember(siteId, personId)
            .then(Mono.just(ResponseEntity.ok("Membre retiré avec succès")))
            .onErrorResume(e -> Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur lors de la suppression du membre : " + e.getMessage())));
    }
    ///////seàrch /////
}
