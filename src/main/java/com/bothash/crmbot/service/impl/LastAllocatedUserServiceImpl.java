package com.bothash.crmbot.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bothash.crmbot.entity.LastAllocatedUser;
import com.bothash.crmbot.repository.LastAllocatedUserRepository;
import com.bothash.crmbot.service.LastAllocatedUserService;

@Service
public class LastAllocatedUserServiceImpl implements LastAllocatedUserService{

	@Autowired
	private LastAllocatedUserRepository lastAllocatedUserRepository;
	
	@Override
	public LastAllocatedUser getFirst() {
		try {
			return lastAllocatedUserRepository.findAll().get(0);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public LastAllocatedUser save(LastAllocatedUser lasUserAllocatedNew) {
		LastAllocatedUser existing=this.getFirst();
		if(existing==null) {
			existing=new LastAllocatedUser();
		}
		existing.setUserId(lasUserAllocatedNew.getUserId());
		return lastAllocatedUserRepository.save(existing);
	}

}
