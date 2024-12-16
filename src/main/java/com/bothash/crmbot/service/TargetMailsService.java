package com.bothash.crmbot.service;

import java.util.List;

import com.bothash.crmbot.entity.TargetMails;

public interface TargetMailsService {

	List<TargetMails> getAll();

	TargetMails save(TargetMails targetMails);

	List<TargetMails> getAllByIsActive(boolean b);

}
