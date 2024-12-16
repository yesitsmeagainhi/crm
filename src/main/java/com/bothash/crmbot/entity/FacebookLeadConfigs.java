package com.bothash.crmbot.entity;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name="facebook_lead_configs")
public class FacebookLeadConfigs implements Serializable{

	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	private String leadId;
	
	private String url;
	
	private String accessToken;
	
	private Long sizeLimit;
	
	private Boolean isActive;
	
	private String campaignName;
	
	private String message;
	
	private String platform;
	
	private LocalDateTime timestamp;
}
