package com.bothash.crmbot.dto;

import java.io.Serializable;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;


@Data
public class FacebookLeadsDto {


	@JsonProperty("id")
	private String id;
	
	@JsonProperty("created_time")
	private String created_time;
	
	@JsonProperty("ad_id")
	private String ad_id;
	
	@JsonProperty("form_id")
	private String form_id;
	
	@JsonProperty("field_data")
	private String field_data;
	
	
	
}
