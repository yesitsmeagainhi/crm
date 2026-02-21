package com.bothash.crmbot.service;

import java.util.List;
import java.util.Optional;

import com.bothash.crmbot.entity.Platforms;


public interface PlatformService {
	
	List<Platforms> getAll();

	Platforms getById(Long parameterId);

	Platforms getBySourceName(String platformName);
	
	Platforms create(Platforms platform);
    Optional<Platforms> update(Long id, Platforms platform);
    boolean delete(Long id);

}
