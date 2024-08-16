package com.xtensus.xteged.service.ldap;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class Unit {


        @JsonProperty("idUnit")
        private Integer idUnit;

        private String nameUnit;
        private String shortNameUnit;

        @JsonIgnoreProperties(value = { "associatedDirection" }, allowSetters = true)
        private Person responsibleUnit;

        @JsonIgnoreProperties(value = { "associatedDirection" }, allowSetters = true)
        private Person secretaryUnit;

        private List<Person> membersUnit;
        private List<com.xtensus.xteged.service.ldap.Unit> listUnitsChildUnit;
        private List<com.xtensus.xteged.service.ldap.Unit> listAdjoiningUnitsUnit;
        private List<BOC> listBOChildUnit;
        private com.xtensus.xteged.service.ldap.Unit associatedUnit;
        private BOC associatedBOC;
        private String descriptionUnit;
        private String rowKeyDirection;
        private List<String> titleUnit;
        private boolean responsibleResponse;

    }


