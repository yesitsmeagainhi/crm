package com.bothash.crmbot.service;

import org.springframework.http.ResponseEntity;

import com.bothash.crmbot.entity.UserMaster;

public interface UserMasterService {

	UserMaster save(UserMaster userMaster);
	
	
	UserMaster getByUserName(String userName);
}
