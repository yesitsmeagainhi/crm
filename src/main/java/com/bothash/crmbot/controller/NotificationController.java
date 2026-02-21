package com.bothash.crmbot.controller;

import com.bothash.crmbot.entity.NotificationToken;
import com.bothash.crmbot.repository.NotificationTokenRepository;

import java.security.Principal;
import java.util.List;

import javax.transaction.Transactional;

import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.keycloak.representations.AccessToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    private NotificationTokenRepository notificationTokenRepository;

    @PostMapping("/save-token")
    @Transactional
    public ResponseEntity<String> saveToken(@RequestBody NotificationToken token, Principal principal) {
        // Save the token to the database
    	KeycloakAuthenticationToken keyToken = (KeycloakAuthenticationToken) principal;
		AccessToken accessToken = keyToken.getAccount().getKeycloakSecurityContext().getToken();
		String userName = accessToken.getPreferredUsername();
		
		List<NotificationToken> existingToken = this.notificationTokenRepository.findByUserName(userName);
		if(!existingToken.isEmpty()) {
			notificationTokenRepository.deleteByUserName(userName);
		}
		token.setUserName(accessToken.getPreferredUsername());
        notificationTokenRepository.save(token);
        return ResponseEntity.ok("Token saved successfully");
    }
}
