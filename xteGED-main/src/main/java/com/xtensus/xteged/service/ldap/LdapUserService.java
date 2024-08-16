package com.xtensus.xteged.service.ldap;


import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.naming.Binding;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
/*
@Service
public class LdapUserService {

    private DirContext dirContext; // Injectez ou initialisez le DirContext ici
    @PostConstruct
    public void init() {
        try {
            Hashtable<String, String> env = new Hashtable<>();
            env.put(DirContext.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
            env.put(DirContext.PROVIDER_URL, "ldap://localhost:389"); // Mettez votre URL LDAP
            // Ajoutez d'autres paramètres d'authentification si nécessaire

            dirContext = new InitialDirContext(env);
        } catch (NamingException e) {
            throw new RuntimeException("Erreur lors de l'initialisation du contexte LDAP", e);
        }
    }
    public List<Person> getAllUsers(int page, int size, String sort, String searchQuery) {
        List<Person> result = new ArrayList<>();
        try {
            int startIndex = page * size;
            NamingEnumeration<Binding> e = dirContext.listBindings("CONTEXT_USER");

            while (e.hasMore()) {
                Binding b = e.next();
                String name = b.getNameInNamespace();

                if (startIndex > 0) {
                    startIndex--;
                    continue;
                }

                if (result.size() >= size) {
                    break;
                }

                Person person = buildPersonFromLdapEntry(name);
                if (matchesSearchQuery(person, searchQuery)) {
                    result.add(person);
                }
            }
        } catch (NamingException e) {
            throw new RuntimeException("Erreur lors de la récupération des utilisateurs LDAP", e);
        }
        return result;
    }

    private Person buildPersonFromLdapEntry(String name) {
        Person person = new Person();
        person.setCn(getLdapEntryAttribute("cn", name));
        person.setId(Integer.parseInt(getLdapEntryAttribute("uid", name)));
        person.setNom(getLdapEntryAttribute("sn", name));
        person.setPrenom(getLdapEntryAttribute("givenName", name));
        person.setShortName(getLdapEntryAttribute("employeeNumber", name));
        person.setNomArabe(getLdapEntryAttribute("businessCategory", name));

        if (getLdapEntryAttribute("roomNumber", name) != null && !getLdapEntryAttribute("roomNumber", name).isEmpty()) {
            person.setUserActive(getLdapEntryAttribute("roomNumber", name));
        } else {
            person.setUserActive("0");
        }

        if (getLdapEntryAttribute("st", name) != null && !getLdapEntryAttribute("st", name).isEmpty()) {
            person.setOtp(getLdapEntryAttribute("st", name));
        } else {
            person.setOtp("0");
        }



        return person;
    }


    public String getLdapEntryAttribute(String attribute, String path) {
        String result = "";

        //        System.out.println("L735 path "+path );
        //        System.out.println("L736 attribute "+attribute );
        try {
            //            System.out.println("L738  res "+dirContext.getAttributes(path)  );
            //            System.out.println("L739  res "+dirContext.getAttributes(path).get(attribute) );

            String res = dirContext.getAttributes(path).get(attribute).get(0).toString();

            if (!res.equals(" ")) {
                result = res;
            } else {
                //System.out.println("!res.equals  "+res);
            }
        } catch (NamingException e) {
            System.out.println("Erreur lors de l'acces au serveur LDAP:" + e);
            e.printStackTrace();
        } catch (NullPointerException ex) {} catch (NumberFormatException ex) {
            result = null;
        }
        return result;
    }




    private boolean matchesSearchQuery(Person person, String searchQuery) {
        if (searchQuery == null || searchQuery.isEmpty()) {
            return true; // Si la requête de recherche est vide, toutes les personnes correspondent
        }

        // Vérifier si ShortName, Nom ou NomArabe contiennent la requête de recherche (ignorer la casse)
        String query = searchQuery.toLowerCase();
        return (
            person.getShortName().toLowerCase().contains(query) ||
                person.getNom().toLowerCase().contains(query) ||
                person.getNomArabe().toLowerCase().contains(query)
        );
    }
    }


*/
