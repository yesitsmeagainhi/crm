package com.bothash.crmbot.dto;

import lombok.Data;

@Data
public class KeycloakUserResponse {
	
	private String id;
	
	private String createdTimestamp;
	
	private String username;
	
	private String enabled;
	
	private String totp;
	
	private String emailVerified;
	
	private String firstName;
	
	private String lastName;
	
	private String email;

}
