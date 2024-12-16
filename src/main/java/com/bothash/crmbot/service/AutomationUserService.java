package com.bothash.crmbot.service;

import java.util.List;

import com.bothash.crmbot.entity.AutomationUsers;

public interface AutomationUserService {

	AutomationUsers getByUserId(String userId);
	AutomationUsers save(AutomationUsers automationUsers);
	 List<AutomationUsers> getAll();
	List<AutomationUsers> getAllActive();
}
