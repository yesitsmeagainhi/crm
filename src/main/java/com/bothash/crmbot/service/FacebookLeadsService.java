package com.bothash.crmbot.service;

import java.util.List;

import com.bothash.crmbot.entity.FacebookLeads;

public interface FacebookLeadsService {
	
	List<FacebookLeads> saveAll(List<FacebookLeads> facebookLeads);

	FacebookLeads save(FacebookLeads facebookLeads);
	
	FacebookLeads getById(Long id);
}
