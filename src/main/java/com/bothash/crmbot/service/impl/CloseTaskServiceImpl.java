package com.bothash.crmbot.service.impl;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.Nullable;
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
		List<CloseTask> closeTask = closeTaskRepository.findByActiveTaskId(taskId);
		if(closeTask!=null && closeTask.size()>0) {
			return closeTask.get(0);
		}
		return null;
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
			Pageable requestedPage,Boolean isAdminLevelData) {
		Page<CloseTask> mytasks=null;
		
		Specification<CloseTask> spec = closeTaskByConvertedAndRoleFilteredUser(
			    true,                    // isConverted
			    role,            // role
			    userName,           // userName
			    filterRequests.getPhoneNumber()                // search string (optional)
			);
		if(isAdminLevelData) {
			filterRequests.setIsConverted(true);
			mytasks=closeTaskRepository.findAll(FilterSpecificationClosedTask.filter(filterRequests),requestedPage);
		}else if(role.equals("manager")) {
			if(filterRequests.getPhoneNumber()!=null && !filterRequests.getPhoneNumber().isEmpty()) {
				mytasks=closeTaskRepository.findAll(spec,requestedPage);
			}else
				mytasks=closeTaskRepository.findByIsConvertedAndActiveTaskManagerName(true,userName,requestedPage);
		}else if(role.equals("telecaller")) {
			if(filterRequests.getPhoneNumber()!=null && !filterRequests.getPhoneNumber().isEmpty()) {
				mytasks=closeTaskRepository.findAll(spec,requestedPage);
			}else
				mytasks=closeTaskRepository.findByIsConvertedAndActiveTaskTelecallerName(true,userName,requestedPage);
		}else if(role.equals("counsellor")) {
			if(filterRequests.getPhoneNumber()!=null && !filterRequests.getPhoneNumber().isEmpty()) {
				mytasks=closeTaskRepository.findAll(spec,requestedPage);
			}else
				mytasks=closeTaskRepository.findByIsConvertedAndActiveTaskCounsellorName(true,userName,requestedPage);
		}
		return mytasks;
	}

	@Override
	public void delete(CloseTask closeTask) {
		closeTaskRepository.delete(closeTask);
	}
	public Specification<CloseTask> closeTaskByConvertedAndRoleFilteredUser(
	        boolean isConverted,
	        String role,
	        String userName,
	        @Nullable String search
	) {
	    return (root, query, cb) -> {
	        List<Predicate> predicates = new ArrayList<>();

	        // Join with ActiveTask
	        Join<CloseTask, ActiveTask> activeTaskJoin = root.join("activeTask");

	        // Converted filter
	        predicates.add(cb.equal(root.get("isConverted"), isConverted));

	        // Role-based userName match
	        String lowerUserName = userName.toLowerCase();
	        if ("telecaller".equalsIgnoreCase(role)) {
	            predicates.add(cb.equal(cb.lower(activeTaskJoin.get("telecallerName")), lowerUserName));
	        } else if ("counsellor".equalsIgnoreCase(role)) {
	            predicates.add(cb.equal(cb.lower(activeTaskJoin.get("counsellorName")), lowerUserName));
	        } else if ("manager".equalsIgnoreCase(role)) {
	            predicates.add(cb.equal(cb.lower(activeTaskJoin.get("managerName")), lowerUserName));
	        }

	        // Optional search on phoneNumber or leadName
	        if (search != null && !search.trim().isEmpty()) {
	            String pattern = "%" + search.toLowerCase() + "%";
	            Predicate phoneMatch = cb.like(cb.lower(activeTaskJoin.get("phoneNumber")), pattern);
	            Predicate leadMatch = cb.like(cb.lower(activeTaskJoin.get("leadName")), pattern);
	            predicates.add(cb.or(phoneMatch, leadMatch));
	        }

	        return cb.and(predicates.toArray(new Predicate[0]));
	    };
	}


}
