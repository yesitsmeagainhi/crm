package com.bothash.crmbot.service;

import java.util.List;

import com.bothash.crmbot.entity.Platforms;


public interface PlatformService {
	
	List<Platforms> getAll();

	Platforms getById(Long parameterId);

	Platforms getBySourceName(String platformName);

}
