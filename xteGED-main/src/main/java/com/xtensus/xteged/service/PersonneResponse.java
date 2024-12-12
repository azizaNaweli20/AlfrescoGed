package com.xtensus.xteged.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.xtensus.xteged.service.person.Company;

public class PersonneResponse {

    private Entry entry;

    public Entry getEntry() {
        return entry;
    }

    public void setEntry(Entry entry) {
        this.entry = entry;
    }

    // Classe interne pour correspondre à la structure de "entry"
    public static class Entry {
        private String id;
        private String firstName;
        private String lastName;
        private String email;
        private String description;
        private boolean emailNotificationsEnabled;
        private Company company;
        private boolean enabled;

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

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public boolean isEmailNotificationsEnabled() {
            return emailNotificationsEnabled;
        }

        public void setEmailNotificationsEnabled(boolean emailNotificationsEnabled) {
            this.emailNotificationsEnabled = emailNotificationsEnabled;
        }

        public Company getCompany() {
            return company;
        }

        public void setCompany(Company company) {
            this.company = company;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }}
