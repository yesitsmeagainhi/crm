package com.bothash.crmbot.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.bothash.crmbot.dto.FilterRequests;
import com.bothash.crmbot.entity.CloseTask;

public interface CloseTaskService {

	CloseTask save(CloseTask closeTask);

	CloseTask getByActiveTask(Long taskId);

	Page<CloseTask> convertedTask(String role, String userName, Pageable requestedPage);

	Page<CloseTask> convertedTask(FilterRequests filterRequests, String role, String userName ,
			Pageable requestedPage);

	void delete(CloseTask closeTask);
}
