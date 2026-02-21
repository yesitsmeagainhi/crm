package com.bothash.crmbot.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

import lombok.Data;

@Data
public class FilterRequests implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

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
	
	private Boolean isScheduledMissed;
	
	private String courseName;
	
	private String status;
	
	private Boolean isOwner;
	
	private String userNameForFilterMainTaskPage;
	
	private String leadType;
	
	private Boolean isSeatConfirmed;

	private Boolean isClaimed;
	
	private Boolean isLeadTransfer;
	
	private String toRole;
	
	private String toUserName;
	
	private Integer numberOfLeads;
	
	private LocalDateTime scheduledTime;
	
	private Boolean isDashboardFilter;
	
	private Boolean hasComments;
	
	private Boolean isDustin;
	
	private String userNameForUi;

	private Boolean noComments;
	
	private Boolean isLeadSummary;
	
	private String dateType;
	
	private Boolean isDateTypeChanged;
}
