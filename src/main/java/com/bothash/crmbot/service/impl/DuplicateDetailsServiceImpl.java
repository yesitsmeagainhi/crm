package com.bothash.crmbot.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bothash.crmbot.entity.DuplicateDetails;
import com.bothash.crmbot.repository.DuplicateDetailsRepository;
import com.bothash.crmbot.service.DuplicateDetailsService;

@Service
public class DuplicateDetailsServiceImpl implements DuplicateDetailsService{

	@Autowired
	private DuplicateDetailsRepository duplicateDetailsRepository;
	
	@Override
	public void save(DuplicateDetails duplicateDetails) {
		duplicateDetailsRepository.save(duplicateDetails);
		
	}

}
