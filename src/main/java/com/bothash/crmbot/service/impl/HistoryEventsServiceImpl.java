package com.bothash.crmbot.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bothash.crmbot.entity.HistoryEvents;
import com.bothash.crmbot.repository.HistoryEventsRepository;
import com.bothash.crmbot.service.HistoryEventsService;

@Service
public class HistoryEventsServiceImpl implements HistoryEventsService{

	@Autowired
	private HistoryEventsRepository historyEventsRepository;
	
	@Override
	public List<HistoryEvents> getByTask(Long id) {
		return historyEventsRepository.findByActiveTaskIdOrderByCreatedOnDesc(id);
	}

	@Override
	public HistoryEvents save(HistoryEvents event) {
		return historyEventsRepository.save(event);
	}
	
	@Override
	public List<HistoryEvents> saveAll(List<HistoryEvents> events) {
		return historyEventsRepository.saveAll(events);
	}

}
