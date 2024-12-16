package com.bothash.crmbot.dto;

import java.util.List;

import lombok.Data;


@Data
public class Groups {
	
	private String id;
	
	private String name;
	
	private String path;
	
	private List<String> subGroups;

}
