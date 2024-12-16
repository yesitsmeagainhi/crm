package com.bothash.crmbot.service;

import java.util.List;

import com.bothash.crmbot.entity.HistoryEvents;

public interface HistoryEventsService {

	List<HistoryEvents> getByTask(Long id);

	HistoryEvents save(HistoryEvents event);
}
