package com.xtensus.xteged.service.ldap;

import com.xtensus.xteged.service.person.Company;

import java.util.List;
import java.util.Map;

public class Person {

    private String id;
    private String firstName;
    private String lastName;
    private String description;
    private String avatarId;
    private String email;
    private String skypeId;
    private String googleId;
    private String instantMessageId;
    private String jobTitle;
    private String location;
    private Company company;
    private String mobile;
    private String telephone;
    private String statusUpdatedAt;
    private String userStatus;
    private boolean enabled;
    private boolean emailNotificationsEnabled;
    private List<String> aspectNames;
    private Map<String, Object> properties;

    public Person() {
    }

    public Person(String id, String firstName, String lastName, String description, String avatarId, String email, String skypeId, String googleId, String instantMessageId, String jobTitle, String location, Company company, String mobile, String telephone, String statusUpdatedAt, String userStatus, boolean enabled, boolean emailNotificationsEnabled, List<String> aspectNames, Map<String, Object> properties) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.description = description;
        this.avatarId = avatarId;
        this.email = email;
        this.skypeId = skypeId;
        this.googleId = googleId;
        this.instantMessageId = instantMessageId;
        this.jobTitle = jobTitle;
        this.location = location;
        this.company = company;
        this.mobile = mobile;
        this.telephone = telephone;
        this.statusUpdatedAt = statusUpdatedAt;
        this.userStatus = userStatus;
        this.enabled = enabled;
        this.emailNotificationsEnabled = emailNotificationsEnabled;
        this.aspectNames = aspectNames;
        this.properties = properties;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAvatarId() {
        return avatarId;
    }

    public void setAvatarId(String avatarId) {
        this.avatarId = avatarId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSkypeId() {
        return skypeId;
    }

    public void setSkypeId(String skypeId) {
        this.skypeId = skypeId;
    }

    public String getGoogleId() {
        return googleId;
    }

    public void setGoogleId(String googleId) {
        this.googleId = googleId;
    }

    public String getInstantMessageId() {
        return instantMessageId;
    }

    public void setInstantMessageId(String instantMessageId) {
        this.instantMessageId = instantMessageId;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getStatusUpdatedAt() {
        return statusUpdatedAt;
    }

    public void setStatusUpdatedAt(String statusUpdatedAt) {
        this.statusUpdatedAt = statusUpdatedAt;
    }

    public String getUserStatus() {
        return userStatus;
    }

    public void setUserStatus(String userStatus) {
        this.userStatus = userStatus;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEmailNotificationsEnabled() {
        return emailNotificationsEnabled;
    }

    public void setEmailNotificationsEnabled(boolean emailNotificationsEnabled) {
        this.emailNotificationsEnabled = emailNotificationsEnabled;
    }

    public List<String> getAspectNames() {
        return aspectNames;
    }

    public void setAspectNames(List<String> aspectNames) {
        this.aspectNames = aspectNames;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }
}
