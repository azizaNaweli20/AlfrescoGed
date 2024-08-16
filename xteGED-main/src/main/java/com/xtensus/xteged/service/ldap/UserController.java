package com.xtensus.xteged.service.ldap;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {
/*
    private final LdapUserService ldapUserService;

    public UserController(LdapUserService ldapUserService) {
        this.ldapUserService = ldapUserService;
    }

    @GetMapping
    public List<Person> getUsers(
        @RequestParam int page,
        @RequestParam int size,
        @RequestParam(required = false) String sort,
        @RequestParam(required = false) String searchQuery) {
        return ldapUserService.getAllUsers(page, size, sort, searchQuery);
    }*/
}

