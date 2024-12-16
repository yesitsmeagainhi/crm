package com.bothash.crmbot.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bothash.crmbot.entity.CounsellingDetails;
import com.bothash.crmbot.repository.CounsellingDetailsRepository;
import com.bothash.crmbot.service.CounsellingDetailsService;

@Service
public class CousellingDetailsServiceImpl implements CounsellingDetailsService{
	
	@Autowired
	private CounsellingDetailsRepository counsellingDetailsRepository;

	@Override
	public CounsellingDetails save(CounsellingDetails counsellingDetails) {
		return counsellingDetailsRepository.save(counsellingDetails);
	}

	@Override
	public List<CounsellingDetails> getByActiveTask(Long id) {
		return counsellingDetailsRepository.findByActiveTaskId(id);
	}

}
