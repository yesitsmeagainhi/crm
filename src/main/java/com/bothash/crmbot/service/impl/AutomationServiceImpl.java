package com.bothash.crmbot.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bothash.crmbot.entity.Automation;
import com.bothash.crmbot.repository.AutomationRepository;
import com.bothash.crmbot.service.AutomationService;

@Service
public class AutomationServiceImpl implements AutomationService{
	
	@Autowired
	private AutomationRepository automationRepository;

	@Override
	public List<Automation> getByIsActive(boolean b) {
		return automationRepository.findByIsActiveOrderByPriority(b);
	}

	@Override
	public Automation save(Automation automation) {
		return automationRepository.save(automation);
	}

	@Override
	public Automation getByParamter(String paramter) {
		return automationRepository.findByParamter(paramter);
	}

}
