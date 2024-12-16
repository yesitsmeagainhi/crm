package com.bothash.crmbot.service;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import com.bothash.crmbot.dto.FilterRequests;
import com.bothash.crmbot.entity.ActiveTask;

public interface ActiveTaskService {
	
	List<ActiveTask> saveAll(List<ActiveTask> tasks);

	Page<ActiveTask> getAllTasks(Pageable pagerequest);

	Page<ActiveTask> getMyTask(String role,String userName, Pageable requestedPage);

	ActiveTask getTaskById(Long taskId);

	ActiveTask save(ActiveTask activeTask);

	Page<ActiveTask> getMyTask(FilterRequests filterRequests, String role, String preferredUsername,
			Pageable requestedPage);

	Page<ActiveTask> getAllTasks(FilterRequests filterRequests, Pageable requestedPage,Boolean isFilter);

	Page<ActiveTask> getManagerTask(String userName, Pageable requestedPage);

	Page<ActiveTask> getTeleCallerTask(String userName, Pageable requestedPage);

	Page<ActiveTask> getAllTasks(Specification<ActiveTask> filter, Pageable requestedPage);

	List<ActiveTask> getAllTasks(Specification<ActiveTask> filter);
	
	Long countOfTotalTask();
	
	Long countOfActiveTask();
	Long countOfTodaysTask();
	Long countOfConvertedTask(Boolean b);
	
	Long countOfTotalTaskByPlatform(String platform);
	
	Long countOfTotalActiveTaskByPlatform(String platform);
	Long countOfTotalConvertedTaskByPlatform(String platform,Boolean isConverted);

	List<ActiveTask> getTaskByPhoneNumber(String phoneNumber);
	
	 Map<String,String> randomlyAssgin(ActiveTask task,  String currentUserRole);


	List<ActiveTask> getByOwner(String role, String userId);

	List<ActiveTask> getByRoleAndOwnerAndSource(String string, String userId, String parameter);

	List<ActiveTask> getByRoleAndOwnerAndCourse(String string, String userId, String parameter);

	Page<ActiveTask> getNotTodaysScheduledTaskByOwner(Pageable requestedPage,String owner);

	Page<ActiveTask> getUnderCounsellingTaskByManager(Pageable requestedPage, String preferredUsername);

	Page<ActiveTask> getNotTodaysScheduledTaskByManager(Pageable requestedPage, String preferredUsername);


	Page<ActiveTask> getNotTodaysScheduledTaskForAdmin(Pageable requestedPage);

	Page<ActiveTask> getTodaysScheduledTaskByOwner(Pageable requestedPage, String preferredUsername);

	Page<ActiveTask> getTodaysScheduledTaskByManager(Pageable requestedPage, String preferredUsername);

	Page<ActiveTask> getTodaysScheduledTaskForAdmin(Pageable requestedPage);

	Page<ActiveTask> getTodaysUnderCounsellingTaskByManager(Pageable requestedPage, String preferredUsername);

	Page<ActiveTask> getNotTodaysUnderCounsellingTaskByManager(Pageable requestedPage2, String preferredUsername);

	Page<ActiveTask> getTodaysUnderCounsellingTaskAdmin(Pageable requestedPage, String preferredUsername);

	Page<ActiveTask> getNotTodaysUnderCounsellingTaskAdmin(Pageable requestedPage2, String preferredUsername);

	Long countOfMyTask(String userName);

	List<ActiveTask> getGraphs(FilterRequests filterRequests);
	
	Long countOfPendingTaskByCaller(String telecallerName);
	
	Long countOfProccessedTaskByCaller(String telecallerName);
	
	Long countOfScheduledTaskByCaller(String telecallerName);
	
	Long countOfCompletedTaskByCaller(String telecallerName);

	Long countOfCounselledTaskByCaller(String string);

}
