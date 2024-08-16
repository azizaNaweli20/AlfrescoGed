package com.xtensus.xteged.domain;
import com.fasterxml.jackson.annotation.JsonProperty;

public class NodeDetailsResponse {


    private NodeDetails entry;

    @JsonProperty("entry")
    public NodeDetails getEntry() {
        return entry;
    }

    public void setEntry(NodeDetails entry) {
        this.entry = entry;
    }

}
