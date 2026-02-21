package com.bothash.crmbot.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import lombok.*;

@Entity @Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class PushSubscription {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	  
	@Column(columnDefinition = "TEXT")
	private String endpoint;      // URL
	  
	private String p256dh;        // base64 key
	  
	private String auth;
	
	private String userName;

}
