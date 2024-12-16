package com.bothash.crmbot.entity;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import lombok.Data;

@Data
@Entity
public class AutomationByCampaign implements Serializable{

	
	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@ManyToOne
	private FacebookLeadConfigs facebookLeadConfigs;
	
	private Boolean isLastAllocated;
	
	private String groupId;
	
	private String groupName;
	
	private Boolean isActive;

}
