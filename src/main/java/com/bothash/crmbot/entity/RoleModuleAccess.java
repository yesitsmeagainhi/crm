package com.bothash.crmbot.entity;

import java.time.LocalDateTime;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import lombok.Data;

@Data
@Entity
public class RoleModuleAccess {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
	
	@ManyToOne
	private Modules modules;
	
	private String role;
	
	private Boolean isEdit;
	
	private Boolean isView;
	
	private Boolean isAdminData;

	@CreationTimestamp
	private LocalDateTime createdOn;
	
	@UpdateTimestamp
	private LocalDateTime modifiedOn;
}
