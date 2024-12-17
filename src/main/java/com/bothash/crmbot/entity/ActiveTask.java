package com.bothash.crmbot.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class ActiveTask implements Serializable {

	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	private String taskName;
	
	private String taskGroup;
	
	private String assignee;
	
	private String owner;
	
	private Boolean	 isClaimed;
	
	private String leadPlatform;
	
	private String campaign;
	
	private Boolean isActive;
	
	private String status;
	
	private Boolean isScheduled;
	
	private String scheduleComment;
	
	private LocalDateTime scheduleTime;
	
	private String schedulerName;
	
	private String schedulerEmail;
	
	private String schedulerUserId;
	
	private String managerName;
	
	private String telecallerName;
	
	private String counsellorName;
	
	private String leadName;
	
	
	private String college;
	
	private String course;
	
	private String area;
	
	private String city;
	
	private String state;
	
	private String requirement;
	
	private String refferenceName;
	
	private String rerefferenceNo;
	
	private Boolean isConverted;
	
	private Boolean isCounsellingDone;
	
	private String closingRemark;
	
	private String remark;
	
	private LocalDateTime claimTime;
	
	private LocalDateTime assignedTime;
	
	private String phoneNumber;
	
	private String phoneNumber2;
	
	private Boolean isDuplicate;
	
	private String leadType;
	
	private Float tenthPercent;
	
	private Float twelethPercent;
	
	private Float neetPercent;
	
	private Boolean isSeatConfirmed;
	
	@ManyToOne
	private FacebookLeads facebookLeads;
	
	@CreationTimestamp
	private LocalDateTime createdOn;
	
	@UpdateTimestamp
	private LocalDateTime modifiedOn;
	
	private String modifiedBy;
	
	private String createdBy;

	
	
}
