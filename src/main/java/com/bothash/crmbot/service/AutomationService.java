package com.bothash.crmbot.service;

import java.util.List;

import com.bothash.crmbot.entity.Automation;

public interface AutomationService {

	List<Automation> getByIsActive(boolean b);

	Automation save(Automation automation);
	
	Automation getByParamter(String paramter);
}
