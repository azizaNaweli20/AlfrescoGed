package com.xtensus.xteged.service.impl;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonBackReference;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xtensus.xteged.service.CmisService;
import com.xtensus.xteged.service.DocumentDetails;
import org.apache.chemistry.opencmis.client.api.*;
import org.apache.chemistry.opencmis.client.runtime.SessionFactoryImpl;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.data.Acl;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.definitions.PropertyDefinition;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinition;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.enums.BindingType;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.exceptions.CmisBaseException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;

import org.springframework.http.ResponseEntity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.nio.file.attribute.AclEntry;
import java.util.*;
@Service


public class CmisServiceImpl implements CmisService {

    private final Logger log = LoggerFactory.getLogger(CmisServiceImpl.class);

    // Set values from "application.properties" file
    @Value("${alfresco.repository.url}")
    String alfrescoUrl;

    @Value("${alfresco.repository.user}")
    String alfrescoUser;

    @Value("${alfresco.repository.pass}")
    String alfrescoPass;
    private final ObjectMapper objectMapper;
    // CMIS living session
    private Session session;
    private static HashMap<String, String> mimeTypeMapping;

    static {
        mimeTypeMapping = new HashMap<String, String>();
        mimeTypeMapping.put("gif", "image/gif");
        mimeTypeMapping.put("jpeg", "image/jpeg");
        mimeTypeMapping.put("jpg", "image/jpeg");
        mimeTypeMapping.put("png", "image/png");
        mimeTypeMapping.put("pdf", "application/pdf");
        mimeTypeMapping.put("docx", "application/docx");
        mimeTypeMapping.put("doc", "application/doc");
        mimeTypeMapping.put("xlsx", "application/xlsx");
        mimeTypeMapping.put("xls", "application/xls");
        mimeTypeMapping.put("csv", "application/csv");
    }

    public CmisServiceImpl(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String getMimeType(String ext) {
        return mimeTypeMapping.get(ext.toLowerCase());
    }

    @PostConstruct
    public void init() {
        try {
            String alfrescoBrowserUrl = alfrescoUrl;
            Map<String, String> parameter = new HashMap<String, String>();
            parameter.put(SessionParameter.USER, alfrescoUser);
            parameter.put(SessionParameter.PASSWORD, alfrescoPass);
            parameter.put(SessionParameter.ATOMPUB_URL, alfrescoBrowserUrl);
            parameter.put(SessionParameter.BINDING_TYPE, BindingType.ATOMPUB.value());
            for (Map.Entry mapentry : parameter.entrySet()) {
                System.out.println("clé: " + mapentry.getKey() + " | valeur: " + mapentry.getValue());
            }
            SessionFactory factory = SessionFactoryImpl.newInstance();
            session = factory.getRepositories(parameter).get(0).createSession();
            Folder root = session.getRootFolder();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Override
    public CmisObject getObject(Session session, Folder parentFolder, String objectName) {
        CmisObject object = null;
        try {
            String path2Object = parentFolder.getPath();
            if (!path2Object.endsWith("/")) {
                path2Object += "/";
            }
            path2Object += objectName;
            object = session.getObjectByPath(path2Object);
        } catch (CmisObjectNotFoundException nfe0) {
            // Nothing to do, object does not exist
        }

        return object;
    }

    @Override
    public Folder createSite(String siteId) {
        try {
            Folder rootFolder = session.getRootFolder();

            Map<String, String> properties = new HashMap<>();
            properties.put(PropertyIds.OBJECT_TYPE_ID, BaseTypeId.CMIS_FOLDER.value());
            properties.put(PropertyIds.NAME, siteId);

            return rootFolder.createFolder(properties);
        } catch (Exception e) {
            log.error("Error creating site", e);
            return null;
        }
    }



    @Override
    public Folder createFolder(String pathName) {
        String[] pathSegments = pathName.split("/");
        Folder parentFolder = session.getRootFolder();

        for (String segment : pathSegments) {
            if (!segment.isEmpty()) {
                try {
                    parentFolder = (Folder) session.getObjectByPath(parentFolder.getPath() + "/" + segment);
                } catch (CmisObjectNotFoundException e) {
                    Map<String, String> folderProps = new HashMap<>();
                    folderProps.put(PropertyIds.OBJECT_TYPE_ID, "cmis:folder");
                    folderProps.put(PropertyIds.NAME, segment);
                    parentFolder = parentFolder.createFolder(folderProps);
                }
            }
        }
        return parentFolder;
    }

    @Override
    public ResponseEntity<String> getNodeDetails(String nodeId) {
        return null;
    }


    @Override
    public Document createDocument(Folder folder, String name, String mimeType, byte[] content) {
        Map<String, String> props = new HashMap<>();
        props.put(PropertyIds.OBJECT_TYPE_ID, "cmis:document");
        props.put(PropertyIds.NAME, name);
        ContentStream contentStream = session.getObjectFactory().createContentStream(name, content.length, mimeType, new ByteArrayInputStream(content));
        return folder.createDocument(props, contentStream, VersioningState.MAJOR);
    }

    @Override
    public Folder GetFolderByPath(String pathName) {
        try {
            return (Folder) session.getObjectByPath(pathName);
        } catch (CmisObjectNotFoundException e) {
            // Log the error or handle it as per your application's requirements
            e.printStackTrace(); // Logging the stack trace for debugging
            return null; // Or throw a custom exception, depending on your error handling strategy
        }
    }

    @Override
    public Folder getRootFolder() {
        return session.getRootFolder();
    }

    @Override
    public Document DownloadDocument(Integer idModel, String idDocument, String model, String extension) {
        try {
            CmisObject doc = session.getObject("workspace://SpacesStore/" + idDocument);
            if (doc instanceof Document) {
                return (Document) doc;
            } else {
                // Log if the document is not found or is of a different type
                log.warn("Document with id {} is not found or is not a Document.", idDocument);
                return null;
            }
        } catch (CmisObjectNotFoundException e) {
            log.error("Document not found: {}", e.getMessage());
            return null;
        } catch (Exception e) {
            log.error("Unexpected error while downloading document: {}", e.getMessage());
            throw new RuntimeException("Error fetching document", e);
        }
    }



    @Override
    public String getDocumentName(String idDocument) {
        Document document = DownloadDocument(null, idDocument, null, null);
        if (document != null) {
            String documentNameWithExtension = document.getName();
            int lastDotIndex = documentNameWithExtension.lastIndexOf('.');

            // Vérifiez que lastDotIndex est valide
            if (lastDotIndex != -1) {
                return documentNameWithExtension.substring(0, lastDotIndex);
            } else {
                // Si le document n'a pas d'extension, retournez le nom tel quel
                return documentNameWithExtension;
            }
        }
        return null;
    }

    @Override
    public DocumentDetails getDocumentDetails(String alfrescoId) {
        if (session == null) {
            return null; // Gérer correctement le cas où la session est nulle
        }

        try {
            CmisObject object = session.getObject(alfrescoId);
            if (object instanceof Document) {
                Document document = (Document) object;
                DocumentDetails details = new DocumentDetails();

                details.setName(document.getName());
                details.setAuthor(document.getCreatedBy()); // Obtention de l'auteur

                // Extraction des propriétés personnalisées
                details.setCreationDate(document.getCreationDate().getTime());
                details.setSize(document.getContentStreamLength());

                // Extraction des propriétés additionnelles
                details.setModifiedAt(document.getLastModificationDate() != null ?
                    document.getLastModificationDate().getTime() : null);


                return details;
            }
        } catch (CmisObjectNotFoundException e) {
            System.err.println("Document non trouvé : " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }



    @Override
    public Folder getFolderByPath(String pathName) {
        try {
            return (Folder) session.getObjectByPath(pathName);
        } catch (CmisObjectNotFoundException e) {
            log.error("Folder not found: {}", pathName, e);
            return null;
        }





    }

/*
    public List<String> getDocumentPermissions(String documentId) {
        try {
            // Get the document object from the CMIS session
            Document document = (Document) session.getObject(session.createObjectId(documentId));

            // Get the ACL for the document
            Acl acl = document.getAcl();

            // Create a list to store the permission strings
            List<String> permissionStrings = new ArrayList<>();

            // Iterate over the ACL entries and add them to the list
            for (AclEntry aclEntry : acl.getEntries()) {
                permissionStrings.add(aclEntry.getPermission().toString());
            }

            return permissionStrings;
        } catch (CmisBaseException e) {
            log.error("Error getting document permissions: {}", e.getMessage());
            return Collections.emptyList();
        }
    }


    public List<String> getDocumentPermissions(String documentId) {
        if (session == null) {
            throw new RuntimeException("CMIS session is not initialized");
        }

        List<String> permissions = new ArrayList<>();

        try {
            Document document = (Document) session.getObject(documentId);
            for (AclEntry ace : document.getAcl().getAces()) {
                permissions.add(ace.getPrincipalId() + ": " + ace.getPermissions());
            }
        } catch (CmisObjectNotFoundException e) {
            log.error("Document not found: " + documentId, e);
            throw new RuntimeException("Document not found: " + documentId, e);
        } catch (Exception e) {
            log.error("Error retrieving permissions for document: " + documentId, e);
            throw new RuntimeException("Error retrieving permissions for document: " + documentId, e);
        }

        return permissions;
    }
*/
}


