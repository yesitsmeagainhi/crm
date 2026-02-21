package com.bothash.crmbot.controller;

import java.security.Principal;

import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.keycloak.representations.AccessToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bothash.crmbot.entity.PushSubscription;
import com.bothash.crmbot.repository.SubscriptionRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/push")
public class PushController {
	
  private final SubscriptionRepository repo;

  @PostMapping("/subscribe")
  public void subscribe(@RequestBody PushSubscription sub,Principal principal) {
	KeycloakAuthenticationToken keyToken = (KeycloakAuthenticationToken) principal;
	AccessToken accessToken = keyToken.getAccount().getKeycloakSecurityContext().getToken();
	String userName = accessToken.getPreferredUsername();
	PushSubscription pushSub = this.repo.findByUserName(userName);
	if(pushSub == null) {
		sub.setUserName(userName);
		repo.save(sub);
	}else {
		pushSub.setAuth(sub.getAuth());
		pushSub.setEndpoint(sub.getEndpoint());
		pushSub.setP256dh(sub.getP256dh());
		repo.save(pushSub);
	}
    
  }
}
