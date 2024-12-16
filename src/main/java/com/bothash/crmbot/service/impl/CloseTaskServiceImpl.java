package com.bothash.crmbot.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.bothash.crmbot.dto.FilterRequests;
import com.bothash.crmbot.entity.ActiveTask;
import com.bothash.crmbot.entity.CloseTask;
import com.bothash.crmbot.repository.CloseTaskRepository;
import com.bothash.crmbot.service.CloseTaskService;
import com.bothash.crmbot.spec.FilterSpecification;
import com.bothash.crmbot.spec.FilterSpecificationClosedTask;

@Service
public class CloseTaskServiceImpl implements CloseTaskService{

	@Autowired
	private CloseTaskRepository closeTaskRepository;
	
	@Override
	public CloseTask save(CloseTask closeTask) {
		return closeTaskRepository.save(closeTask);
	}

	@Override
	public CloseTask getByActiveTask(Long taskId) {
		return closeTaskRepository.findByActiveTaskId(taskId);
	}

	@Override
	public Page<CloseTask> convertedTask(String role, String userName, Pageable requestedPage) {
		Page<CloseTask> mytasks=null;
		if(role.equals("admin")) {
			mytasks=closeTaskRepository.findByIsConverted(true,requestedPage);
		}else if(role.equals("manager")) {
			mytasks=closeTaskRepository.findByIsConvertedAndActiveTaskManagerName(true,userName,requestedPage);
		}else if(role.equals("telecaller")) {
			mytasks=closeTaskRepository.findByIsConvertedAndActiveTaskTelecallerName(true,userName,requestedPage);
		}else if(role.equals("counsellor")) {
			mytasks=closeTaskRepository.findByIsConvertedAndActiveTaskCounsellorName(true,userName,requestedPage);
		}
		return mytasks;
	}

	@Override
	public Page<CloseTask> convertedTask(FilterRequests filterRequests, String role, String userName, 
			Pageable requestedPage) {
		Page<CloseTask> mytasks=null;
		if(role.equals("admin")) {
			filterRequests.setIsConverted(true);
			mytasks=closeTaskRepository.findAll(FilterSpecificationClosedTask.filter(filterRequests),requestedPage);
		}else if(role.equals("manager")) {
			mytasks=closeTaskRepository.findByIsConvertedAndActiveTaskManagerName(true,userName,requestedPage);
		}else if(role.equals("telecaller")) {
			mytasks=closeTaskRepository.findByIsConvertedAndActiveTaskTelecallerName(true,userName,requestedPage);
		}else if(role.equals("counsellor")) {
			mytasks=closeTaskRepository.findByIsConvertedAndActiveTaskCounsellorName(true,userName,requestedPage);
		}
		return mytasks;
	}

	@Override
	public void delete(CloseTask closeTask) {
		closeTaskRepository.delete(closeTask);
	}

}
