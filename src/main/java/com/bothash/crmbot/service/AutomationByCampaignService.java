package com.bothash.crmbot.service;

import java.util.List;
import java.util.Map;

import com.bothash.crmbot.entity.AutomationByCampaign;

public interface AutomationByCampaignService {

	AutomationByCampaign getByCampaignIdAndGroupId(Long campaignId,String groupId);

	AutomationByCampaign save(AutomationByCampaign automationByCampaign);

	List<AutomationByCampaign> getByCampaignId(Long id);
	
	 Map<String,String> allocate(String courseId);
	 
	 Long delete(Long id);
}
