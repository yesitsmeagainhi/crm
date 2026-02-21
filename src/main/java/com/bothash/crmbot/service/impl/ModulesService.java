package com.bothash.crmbot.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bothash.crmbot.entity.Modules;
import com.bothash.crmbot.repository.ModulesRepository;

@Service
public class ModulesService {
	
	@Autowired
	private ModulesRepository modulesRepository;

	public List<Modules> getAll() {
		return modulesRepository.findAll();
	}

}
