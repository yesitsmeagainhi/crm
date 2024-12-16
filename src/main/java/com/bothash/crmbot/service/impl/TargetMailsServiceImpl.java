package com.bothash.crmbot.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bothash.crmbot.entity.TargetMails;
import com.bothash.crmbot.repository.TargetMailsRepository;
import com.bothash.crmbot.service.TargetMailsService;

@Service
public class TargetMailsServiceImpl implements TargetMailsService{

	@Autowired
	private TargetMailsRepository targetMailsRepository;
	
	@Override
	public List<TargetMails> getAll() {
		return targetMailsRepository.findAll();
	}

	@Override
	public TargetMails save(TargetMails targetMails) {
		return targetMailsRepository.save(targetMails);
	}

	@Override
	public List<TargetMails> getAllByIsActive(boolean b) {
		return targetMailsRepository.findByIsActive(b);
	}

}
