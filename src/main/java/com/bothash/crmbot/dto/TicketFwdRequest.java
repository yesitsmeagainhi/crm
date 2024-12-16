package com.bothash.crmbot.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TicketFwdRequest {
	
	private String userEmail;
	
	private String userName;
	
	private String userGroup;
	
	private Long taskId;
	
	private String status;
	
	private String forwarderUserName;
	
	private String forwarderUserEmail;
	
	private String forwarderUserId;
	
	private String remark;
}
