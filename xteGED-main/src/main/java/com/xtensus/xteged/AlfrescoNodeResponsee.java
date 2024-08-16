package com.xtensus.xteged;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AlfrescoNodeResponsee {
    private String id;
    private String name;
    private String nodeType;
    private boolean isFolder;
    private boolean isFile;
    private boolean isLocked;
    private String modifiedAt;
    private UserInfo modifiedByUser;
    private String createdAt;
    private UserInfo createdByUser;
    private String parentId;
    private boolean isLink;
    private boolean isFavorite;
    private ContentInfo content;
    private List<String> aspectNames;
    private Object properties; // Update to actual type if known
    private List<String> allowableOperations;
    private PathInfo path;
    private PermissionsInfo permissions;
    private DefinitionInfo definition;

    // Getters and setters
}

class UserInfo {
    private String displayName;
    private String id;

    // Getters and setters
}

class ContentInfo {
    private String mimeType;
    private String mimeTypeName;
    private long sizeInBytes;
    private String encoding;

    // Getters and setters
}

class PathInfo {
    private List<PathElement> elements;
    private String name;
    private boolean isComplete;

    // Getters and setters
}

class PathElement {
    private String id;
    private String name;
    private String nodeType;
    private List<String> aspectNames;

    // Getters and setters
}

class PermissionsInfo {
    private boolean isInheritanceEnabled;
    private List<AuthorityInfo> inherited;
    private List<AuthorityInfo> locallySet;
    private List<String> settable;

    // Getters and setters
}

class AuthorityInfo {
    private String authorityId;
    private String name;
    private String accessStatus;

    // Getters and setters
}

class DefinitionInfo {
    private List<PropertyInfo> properties;

    // Getters and setters
}

class PropertyInfo {
    private String id;
    private String title;
    private String description;
    private String defaultValue;
    private String dataType;
    private boolean isMultiValued;
    private boolean isMandatory;
    private boolean isMandatoryEnforced;
    private boolean isProtected;
    private List<ConstraintInfo> constraints;

    // Getters and setters
}

class ConstraintInfo {
    private String id;
    private String type;
    private String title;
    private String description;
    private Object parameters; // Update to actual type if known

    // Getters and setters
}

