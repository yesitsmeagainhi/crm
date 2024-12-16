package com.bothash.crmbot.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bothash.crmbot.entity.AutomationUsers;
import com.bothash.crmbot.repository.AutomationUsersRepository;
import com.bothash.crmbot.service.AutomationUserService;

@Service
public class AutomationUserServiceImpl implements AutomationUserService {

	@Autowired
	private AutomationUsersRepository automationUsersRepository;

	@Override
	public AutomationUsers getByUserId(String userId) {
		return automationUsersRepository.findByUserIdAndIsActive(userId,true);
	}

	@Override
	public AutomationUsers save(AutomationUsers automationUsers) {
		return automationUsersRepository.save(automationUsers);
	}

	@Override
	public List<AutomationUsers> getAll() {
		return automationUsersRepository.findAll();
	}

	@Override
	public List<AutomationUsers> getAllActive() {
		return automationUsersRepository.findByIsActive(true);
	}
	
	
}
