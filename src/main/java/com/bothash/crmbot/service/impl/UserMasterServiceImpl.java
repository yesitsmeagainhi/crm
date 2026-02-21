package com.bothash.crmbot.service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bothash.crmbot.entity.UserMaster;
import com.bothash.crmbot.repository.UserMasterRepository;
import com.bothash.crmbot.service.UserMasterService;

@Service
public class UserMasterServiceImpl implements UserMasterService{

	@Autowired
	private UserMasterRepository userMasterRepository;
	
	@Override
	public UserMaster save(UserMaster userMaster) {
		UserMaster existing=userMasterRepository.findByUserName(userMaster.getUserName());
		if(existing!=null) {
			userMaster.setId(existing.getId());
		}
		return userMasterRepository.save(userMaster);
	}

	@Override
	public UserMaster getByUserName(String userName) {
		return userMasterRepository.findByUserName(userName);
	}

	@Override
	public Optional<UserMaster> findById(Long id) {
		return this.userMasterRepository.findById(id);
	}

	@Override
	public boolean existsById(Long id) {
		return this.userMasterRepository.existsById(id);
	}

	@Override
	public void deleteById(Long id) {
		 this.userMasterRepository.deleteById(id);
		
	}

	@Override
	public List<UserMaster> findAll() {
		return this.userMasterRepository.findAll();
	}

}
