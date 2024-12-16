package com.bothash.crmbot.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class FilterRequests {

	private String leadPlatform;
	
	private String role;
	
	private String userName;
	
	private Boolean isConverted;
	
	private Boolean isMyTask;
	
	private Boolean isAllTask;
	
	private Boolean isCompletedTask;
	
	private Boolean isAdmin;
	private Boolean isManager;
	private Boolean isTeleCaller;
	private Boolean isCounsellor;
	
	private String assignee;
	
	private String fromDate;
	
	private String toDate;
	
	private Boolean isActive;
	
	private String leadName;
	
	private String phoneNumber;
	
	private Boolean isCounselled;
	
	private Boolean isScheduled;
	
	private String courseName;
	
	private String status;
	
	private Boolean isOwner;
	
	private String userNameForFilterMainTaskPage;
	
	private String leadType;
}
