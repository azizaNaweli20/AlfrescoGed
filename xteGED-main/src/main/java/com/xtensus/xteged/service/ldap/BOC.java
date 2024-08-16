package com.xtensus.xteged.service.ldap;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;


import java.util.List;

public class BOC implements java.io.Serializable {

        private static final long serialVersionUID = 3497435991347144840L;
        private int idBOC;
        private String nameBOC;
        private String shortNameBOC;
        private List<BOC> listChildBOCsBOC;
        private List<BOC> listAdjoiningBOCsBOC;

        @JsonIgnore
        private List<Unit> listDirectionsChildBOC;

        @JsonInclude
        private List<Person> membersBOC;

        private List<Person> responsiblBOC;
        private String typeBOC;
        private String descriptionBOC;
        private String rowKeyBOC;

        private Unit associatedDirection;

    }



