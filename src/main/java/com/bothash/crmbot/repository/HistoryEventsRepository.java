package com.bothash.crmbot.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bothash.crmbot.entity.HistoryEvents;

public interface HistoryEventsRepository extends JpaRepository<HistoryEvents, Long>{
	
	List<HistoryEvents> findByActiveTaskId(Long id);

	List<HistoryEvents> findByActiveTaskIdOrderByCreatedOnDesc(Long id);

}
