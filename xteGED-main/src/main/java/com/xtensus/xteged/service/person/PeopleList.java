package com.xtensus.xteged.service.person;

import java.util.List;

public class PeopleList {

    private Pagination pagination;
    private List<PersonEntry> entries;

    public Pagination getPagination() {
        return pagination;
    }

    public void setPagination(Pagination pagination) {
        this.pagination = pagination;
    }

    public List<PersonEntry> getEntries() {
        return entries;
    }

    public void setEntries(List<PersonEntry> entries) {
        this.entries = entries;
    }
}
