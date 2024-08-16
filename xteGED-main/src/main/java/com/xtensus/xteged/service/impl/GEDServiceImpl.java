package com.xtensus.xteged.service.impl;

import com.xtensus.xteged.service.CmisService;
import com.xtensus.xteged.service.GEDService;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GEDServiceImpl implements GEDService {

    private final Logger log = LoggerFactory.getLogger(GEDServiceImpl.class);

    @Autowired
    private CmisService cmisService;

    @Override
    public String saveDocumentToAlfrescoInPath(byte[] files, String pathName, String fileName, String extension) {
        Folder folder = cmisService.GetFolderByPath(pathName);
        // create document
        org.apache.chemistry.opencmis.client.api.Document docum = cmisService.createDocument(folder, fileName, extension, files);
        String idAlfresco = docum.getId();
        String alfrescoId = idAlfresco.substring(24, idAlfresco.length());
        return alfrescoId;
    }
}
