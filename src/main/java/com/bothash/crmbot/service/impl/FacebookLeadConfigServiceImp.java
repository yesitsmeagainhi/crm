package com.bothash.crmbot.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bothash.crmbot.entity.FacebookLeadConfigs;
import com.bothash.crmbot.repository.FacebookLeadConfigRepository;
import com.bothash.crmbot.service.FacebookLeadConfigService;

@Service
public class FacebookLeadConfigServiceImp implements FacebookLeadConfigService{

	@Autowired
	private FacebookLeadConfigRepository facebookLeadConfigRepository;
	
	@Override
	public List<FacebookLeadConfigs> getAllActiveCongifs(String platform) {
		List<FacebookLeadConfigs> activeConfigs=facebookLeadConfigRepository.findByIsActiveAndPlatform(true,platform);
		return activeConfigs;
	}

	@Override
	public FacebookLeadConfigs save(FacebookLeadConfigs activeConfigs) {
		return facebookLeadConfigRepository.save(activeConfigs);
	}

	@Override
	public List<FacebookLeadConfigs> getAll() {
		return facebookLeadConfigRepository.findAll();
	}
	
	@Override
	public List<FacebookLeadConfigs> getAllByIsActive(Boolean isActive) {
		return facebookLeadConfigRepository.findAllByIsActive(isActive);
	}

	@Override
	public FacebookLeadConfigs getByCampaignName(String campaignName) {
		return facebookLeadConfigRepository.findByIsActiveAndCampaignName(true,campaignName);
	}

	@Override
	public FacebookLeadConfigs getById(Long parameterId) {
		return facebookLeadConfigRepository.findById(parameterId).get();
	}

}
