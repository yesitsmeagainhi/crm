package com.bothash.crmbot.dto;

import lombok.Data;

@Data
public class DashboardBasicResponse {
	
	private String userName;
	
	private String userId;
	
	private Integer todaysScheduled;
	
	private Integer missedSchedule;
	
	private Integer counselled;
	
	private Integer converted;
	
	private Integer total;
	
	private Integer dustbin;
	
	private Integer blank;
	
	private Integer hot;
	
	private Integer prospect;
	
	private Integer cold;
	
	private Integer noComment;
	
	private Integer noSchedule;
	
	private String image;

}
