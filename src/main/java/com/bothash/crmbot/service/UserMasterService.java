package com.bothash.crmbot.service;

import java.util.List;
import java.util.Optional;

import org.springframework.http.ResponseEntity;

import com.bothash.crmbot.entity.UserMaster;

public interface UserMasterService {

	UserMaster save(UserMaster userMaster);
	
	
	UserMaster getByUserName(String userName);


	Optional<UserMaster> findById(Long id);


	boolean existsById(Long id);


	void deleteById(Long id);


	List<UserMaster> findAll();
}
