package com.xtensus.xteged.service;

import java.util.List;

public class DocumentPermission {
    private String principalId;
    private List<String> permissions;

    public String getPrincipalId() {
        return principalId;
    }

    public void setPrincipalId(String principalId) {
        this.principalId = principalId;
    }

    public List<String> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<String> permissions) {
        this.permissions = permissions;
    }
}
