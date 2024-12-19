package com.xtensus.xteged.web.rest;

import com.xtensus.xteged.service.CmisService;
import com.xtensus.xteged.service.GEDService;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;
import com.xtensus.xteged.service.CmisService;
import com.xtensus.xteged.service.DocumentDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.exceptions.CmisBaseException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

/**
 * GEDControllerResource controller
 */


@CrossOrigin(origins = "http://localhost:4200")
@RestController

@RequestMapping("/api/ged-controller")
public class GEDControllerResource {

    @Autowired
    private CmisService cmisService;

    @Autowired
    private GEDService gEDService;

    private final Logger log = LoggerFactory.getLogger(GEDControllerResource.class);

    /**
     * POST uploadDocument
     */

    @PostMapping("/upload")
    public Mono<String> uploadFile(@RequestParam("pathName") String pathName,
                                   @RequestParam("fileName") String fileName,
                                   @RequestParam("file") MultipartFile multipartFile) throws IOException {
        byte[] fileContent = multipartFile.getBytes();
        Folder folder = cmisService.getFolderByPath(pathName);

        if (folder == null) {
            // Créer le dossier si inexistant
            folder = cmisService.createFolder(pathName);
        }

        Document document = cmisService.createDocument(folder, fileName, multipartFile.getContentType(),
            fileContent);
        return Mono.just("Document uploaded: " + document.getId());
    }
   /* @PostMapping(value = "/upload-document", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadDocument(
        @RequestParam(value = "pathName", required = true) String pathName,
        @RequestParam(value = "fileName", required = true) String fileName,
        @RequestPart(value = "file", required = true) MultipartFile files
    ) {
        System.out.println("Ca commence");
        LinkedHashMap<String, String> msg = new LinkedHashMap<String, String>();
        try {
            String extension = files.getOriginalFilename().substring(files.getOriginalFilename().lastIndexOf(".") + 1);
            fileName += "." + extension;

            String alfrescoId = gEDService.saveDocumentToAlfrescoInPath(files.getBytes(), pathName, fileName, extension);
            return new ResponseEntity<>(alfrescoId, HttpStatus.OK);
        } catch (IOException e) {
            msg.put("message", "Documnt n'a pas Uploade");
            e.printStackTrace();
            return new ResponseEntity<>("Documnt Saved ", HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            msg.put("message", "Documnt n'a pas Uploadeddd");
            e.printStackTrace();

            return new ResponseEntity<>("Documnt n'a pas Uploaded", HttpStatus.BAD_REQUEST);
        }
    }*/
  /*  @PostMapping(value = "/upload-document", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadDocument(
        @RequestParam(value = "pathName", required = true) String pathName,
        @RequestParam(value = "fileName", required = true) String fileName,
        @RequestPart(value = "file", required = true) MultipartFile file
    ) {
        try {
            // Récupérer l'extension du fichier d'origine
            String extension = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf(".") + 1);

            // Ajouter l'extension au nom de fichier fourni
            fileName += "." + extension;

            // Appeler le service pour uploader le document
            String alfrescoId = cmisService.uploadDocument(file.getBytes(), pathName, fileName, extension);

            // Retourner l'ID d'Alfresco en cas de succès
            return new ResponseEntity<>(alfrescoId, HttpStatus.OK);
        } catch (IOException e) {
            // Imprimer la trace de l'exception pour le debugging
            e.printStackTrace();
            // Retourner une réponse BAD_REQUEST en cas d'erreur d'IO
            return new ResponseEntity<>("Document n'a pas été uploadé", HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            // Imprimer la trace de l'exception pour le debugging
            e.printStackTrace();
            // Retourner une réponse BAD_REQUEST pour toute autre exception
            return new ResponseEntity<>("Document n'a pas été uploadé", HttpStatus.BAD_REQUEST);
        }
    }*/

    @GetMapping(value = "/downloadDocumentByAlfrescoId/{alfrescoId}")
    public ResponseEntity<Object> downloadDocumentByAlfrescoId(@PathVariable("alfrescoId") String alfrescoId) {
        try {
            org.apache.chemistry.opencmis.client.api.Document document = cmisService.DownloadDocument(null, alfrescoId, null, null);

            if (document == null) {
                return ResponseEntity.notFound().build();
            }

            ContentStream content = document.getContentStream();
            if (content == null) {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Le document n'a pas de contenu.");
            }

            String mimeType = document.getContentStreamMimeType();
            String name = document.getName();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(mimeType));
            headers.setContentDisposition(ContentDisposition.builder("attachment").filename(name).build());

            return ResponseEntity.ok()
                .headers(headers)
                .contentLength(content.getLength())
                .body(new InputStreamResource(content.getStream()));

        } catch (Exception e) {
            log.error("Erreur lors du téléchargement du document: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur inattendue");
        }
    }

    @GetMapping(value = "/getDocumentNameByAlfrescoId/{alfrescoId}")
    public ResponseEntity<String> getDocumentNameByAlfrescoId(@PathVariable("alfrescoId") String alfrescoId) {
        String documentName = cmisService.getDocumentName(alfrescoId);
        return ResponseEntity.ok(documentName);
    }



}


