package com.bothash.crmbot.controller;

import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.keycloak.representations.AccessToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

import javax.annotation.security.RolesAllowed;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@RestController
@RequestMapping("/auth")
public class KeycloakController {

    @GetMapping("/logout")
    public String logout(Principal principal,HttpServletRequest request) {
        KeycloakAuthenticationToken token = (KeycloakAuthenticationToken) principal;
        
        AccessToken accessToken = token.getAccount().getKeycloakSecurityContext().getToken();
        try {
			request.logout();
		} catch (ServletException e) {
			e.printStackTrace();
		}
        return accessToken.getPreferredUsername()+" successfully logged out";
    }
}