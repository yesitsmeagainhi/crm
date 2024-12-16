package com.bothash.crmbot.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class SchedulerRequest {

	private Long taskId;
	
	private LocalDateTime scheduleTime;
	
	private String schedulerName;
	
	private String schedulerEmail;
	
	private String schedulerUserId;
	
	private String comment;
	
}
