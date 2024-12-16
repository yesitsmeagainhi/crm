package com.bothash.crmbot.dto;

import java.sql.Date;
import java.sql.Time;

import lombok.Data;

@Data
public class JustDialCreateRequest {
	
	private String leadid;
	
	private String leadtype;
	
	private String prefix;
	
	private String name;
	
	private String mobile;
	
	private String phone;
	
	private String email;
	
	private Date date;
	
	private String category;
	private String city;
	private String area;
	private String brancharea;
	private int dncmobile;
	private int dncphone;
	private String company;
	private String pincode;
	private Time time;
	private String branchpin;
	private String parentid;
	
	

}
