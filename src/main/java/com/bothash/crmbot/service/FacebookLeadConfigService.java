package com.bothash.crmbot.service;

import java.util.List;

import com.bothash.crmbot.entity.FacebookLeadConfigs;

public interface FacebookLeadConfigService {

	List<FacebookLeadConfigs> getAllActiveCongifs(String platform);

	FacebookLeadConfigs save(FacebookLeadConfigs activeConfig);
	
	List<FacebookLeadConfigs> getAll();

	FacebookLeadConfigs getByCampaignName(String campaignName);

	FacebookLeadConfigs getById(Long parameterId);
	
	List<FacebookLeadConfigs> getAllByIsActive(Boolean isActive);
}
