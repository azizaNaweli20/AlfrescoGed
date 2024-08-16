package com.xtensus.xteged.service;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.Session;
import org.springframework.http.ResponseEntity;

import java.util.Map;

public interface CmisService {
    //create dococument Final
    Document createDocument(
        org.apache.chemistry.opencmis.client.api.Folder folder,
        String documentName,
        String contentType,
        byte[] content
    );

    public Folder GetFolderByPath(String pathName);

    public Folder getRootFolder();

    public Document DownloadDocument(Integer idModel, String idDocument, String model, String extension);

    String getDocumentName(String idDocument);


    DocumentDetails getDocumentDetails(String alfrescoId);

    Folder getFolderByPath(String pathName);

    CmisObject getObject(Session session, Folder parentFolder, String objectName);

    Folder createSite(String siteId);

    Folder createFolder(String pathName);


    ResponseEntity<String> getNodeDetails(String nodeId);



}
