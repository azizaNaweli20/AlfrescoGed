package com.xtensus.xteged.domain;
import com.fasterxml.jackson.annotation.JsonProperty;
public class Properties {


    @JsonProperty("qshare:sharedBy")
    private String sharedBy;

    @JsonProperty("cm:versionType")
    private String versionType;

    @JsonProperty("cm:versionLabel")
    private String versionLabel;

    @JsonProperty("cm:author")
    private String author;

    @JsonProperty("cm:lastThumbnailModification")
    private String[] lastThumbnailModification;

    @JsonProperty("qshare:sharedId")
    private String sharedId;

    // Getters and setters for all fields

}
