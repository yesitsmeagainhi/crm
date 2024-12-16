package com.bothash.crmbot.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.CreationTimestamp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class HistoryEvents implements Serializable{

	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	private String event;
	
	private String remark;
	
	private String userName;
	
	private String userEmail;
	
	private String userId;
		
	@ManyToOne
	private ActiveTask activeTask;
	
	@CreationTimestamp
	private LocalDateTime createdOn;
	
	private String modifiedBy;
	
	private String createdBy;

}
