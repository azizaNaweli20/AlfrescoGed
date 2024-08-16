package com.xtensus.xteged.service;

public class PermissionRequest {
    private String authority;
    private String permission;

    public PermissionRequest(String authority, String permission) {
        this.authority = authority;
        this.permission = permission;
    }

    public String getAuthority() {
        return authority;
    }

    public void setAuthority(String authority) {
        this.authority = authority;
    }

    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }
}


