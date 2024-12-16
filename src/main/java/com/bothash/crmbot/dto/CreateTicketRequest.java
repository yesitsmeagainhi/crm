package com.bothash.crmbot.dto;

import com.bothash.crmbot.entity.ActiveTask;
import com.bothash.crmbot.entity.FacebookLeads;

import lombok.Data;

@Data
public class CreateTicketRequest {

	private FacebookLeads facebookLeads;
	
	private String leadName;
	
	private ActiveTask activeTask;
	
	private String userName;
	
	private String userEmail;
	
	private String userId;
	
	private String phoneNumber;
	
	private Boolean assignToMe;
	
	private String ownerName;
}
