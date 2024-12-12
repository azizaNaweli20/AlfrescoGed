package com.xtensus.xteged.web.rest.vm;

import com.xtensus.xteged.service.ProcessService;
import com.xtensus.xteged.service.ProcessVariables;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;


@RestController
@RequestMapping("/api/ged-controller")
public class ProcessController {


@Autowired
private ProcessService processService ;


    // Endpoint pour obtenir  un processus

    @GetMapping("/processes")
    public Mono<ResponseEntity<String>> getProcesses(
        @RequestParam(value = "skipCount", defaultValue = "0") int skipCount,
        @RequestParam(value = "maxItems", defaultValue = "10") int maxItems,
        @RequestParam(value = "properties", required = false) String[] properties,
        @RequestParam(value = "orderBy", required = false) String[] orderBy,
        @RequestParam(value = "where", required = false) String where) {

        return processService.getProcesses(skipCount, maxItems, properties, orderBy, where)
            .map(response -> ResponseEntity.ok(response))
            .onErrorResume(e -> Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage())));
    }
    // Endpoint pour delete un processus
    @DeleteMapping("/processes/{processId}")
    public Mono<ResponseEntity<Object>> deleteProcess(@PathVariable("processId") String processId) {
        return processService.deleteProcess(processId)
            .then(Mono.just(ResponseEntity.noContent().build()))
            .onErrorResume(e -> Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to delete process: " + e.getMessage())));
    }

    // Endpoint pour créer un processus


    @PostMapping("/processes")
    public Mono<ResponseEntity<String>> createProcess(
        @RequestParam String processDefinitionKey,
        @RequestBody ProcessVariables variables) {

        return processService.createProcess(processDefinitionKey, variables)
            .map(response -> ResponseEntity.status(HttpStatus.CREATED).body(response))
            .onErrorResume(error -> Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error.getMessage())));
    }

/*
    @PostMapping("/start")
    public Mono<ResponseEntity<String>> startWorkflow(
        @RequestParam String processDefinitionKey,
        @RequestBody WorkflowVariables variables) {

        return workflowService.startWorkflow(processDefinitionKey, variables)
            .map(response -> ResponseEntity.status(HttpStatus.CREATED).body(response))
            .onErrorResume(error -> Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error.getMessage())));
    }

*/

}
