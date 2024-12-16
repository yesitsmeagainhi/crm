package com.bothash.crmbot.service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bothash.crmbot.entity.FacebookLeads;
import com.bothash.crmbot.repository.FacebookLeadsRepostory;
import com.bothash.crmbot.service.FacebookLeadsService;

@Service
public class FacebookLeadsServiceImpl implements FacebookLeadsService{

	@Autowired
	private FacebookLeadsRepostory facebookLeadsRepostory;
	
	@Override
	public List<FacebookLeads> saveAll(List<FacebookLeads> facebookLeads) {
		return facebookLeadsRepostory.saveAll(facebookLeads);
	}

	@Override
	public FacebookLeads save(FacebookLeads facebookLeads) {
		return facebookLeadsRepostory.save(facebookLeads);
	}

	@Override
	public FacebookLeads getById(Long id) {
		Optional<FacebookLeads> optFacebookLead=facebookLeadsRepostory.findById(id);
		if(optFacebookLead.isPresent()) {
			return optFacebookLead.get();
		}
		return null;
	}

}
