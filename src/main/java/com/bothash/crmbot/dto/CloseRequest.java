package com.bothash.crmbot.dto;

import lombok.Data;

@Data
public class CloseRequest {
	
	private Long taskId;

	private String remark;
	
	private Boolean isConverted;
	
	private Boolean isTaskCompleted;

	private Boolean isSeatConfirmed;
	
	private Boolean closeTask;
	
	private String userEmail;
	
	private String userName;
	
	private String userId;
	
	private String counsellingDoneBy;
	
	private String admissionDoneBy;
}
