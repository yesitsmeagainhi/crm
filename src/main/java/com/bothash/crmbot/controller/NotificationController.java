package com.bothash.crmbot.controller;

import com.bothash.crmbot.entity.CrmNotification;
import com.bothash.crmbot.entity.NotificationToken;
import com.bothash.crmbot.repository.CrmNotificationRepository;
import com.bothash.crmbot.repository.NotificationTokenRepository;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    @Autowired
    private CrmNotificationRepository crmNotificationRepository;

    @PostMapping("/save-token")
    @Transactional
    public ResponseEntity<String> saveToken(@RequestBody NotificationToken token, Principal principal) {
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

    @GetMapping("/unread")
    public ResponseEntity<List<CrmNotification>> getUnreadNotifications(Principal principal) {
        String userName = getUserName(principal);
        List<CrmNotification> notifications = crmNotificationRepository
                .findByRecipientUserNameAndIsReadFalseOrderByCreatedOnDesc(userName);
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(Principal principal) {
        String userName = getUserName(principal);
        long count = crmNotificationRepository.countByRecipientUserNameAndIsReadFalse(userName);
        Map<String, Long> response = new HashMap<>();
        response.put("count", count);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/mark-read")
    @Transactional
    public ResponseEntity<String> markAllAsRead(Principal principal) {
        String userName = getUserName(principal);
        List<CrmNotification> unread = crmNotificationRepository
                .findByRecipientUserNameAndIsReadFalseOrderByCreatedOnDesc(userName);
        for (CrmNotification notification : unread) {
            notification.setIsRead(true);
        }
        crmNotificationRepository.saveAll(unread);
        return ResponseEntity.ok("Notifications marked as read");
    }

    private String getUserName(Principal principal) {
        KeycloakAuthenticationToken keyToken = (KeycloakAuthenticationToken) principal;
        AccessToken accessToken = keyToken.getAccount().getKeycloakSecurityContext().getToken();
        return accessToken.getPreferredUsername();
    }
}
