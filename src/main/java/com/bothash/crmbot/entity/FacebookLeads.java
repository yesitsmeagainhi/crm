package com.bothash.crmbot.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FacebookLeads implements Serializable{

private static final long serialVersionUID = 1L;
	
	@Id
	@JsonProperty("id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@CreationTimestamp
	private LocalDateTime createdOn;
	
	@UpdateTimestamp
	private LocalDateTime modifiedOn;
	
	private String modifiedBy;
	
	private String leadCreationTime;
	
	private String adId;
	
	private String formId;
	
	@Column(columnDefinition = "TEXT")
	private String fieldData;
	
	private String leadId;
	
	
	
}
