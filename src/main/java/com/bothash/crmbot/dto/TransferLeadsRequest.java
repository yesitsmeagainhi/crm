package com.bothash.crmbot.dto;

import lombok.Data;

@Data
public class TransferLeadsRequest {
	
	private String fromUserName;
	
	private String toUserName;
	
	private String fromRole;
	
	private String course;
	
	private String platform;
	
	private String leadType;
	
	private String toRole;

	private Integer numberOfLeads;
}
