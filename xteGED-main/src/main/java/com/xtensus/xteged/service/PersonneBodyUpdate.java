package com.xtensus.xteged.service;

import com.xtensus.xteged.service.person.Company;

public class PersonneBodyUpdate {
    private String firstName;
    private String lastName;
    private String description;
    private String email;
    private Company company;

    public PersonneBodyUpdate() {
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }





}
