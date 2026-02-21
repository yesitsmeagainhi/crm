package com.bothash.crmbot.service;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.criteria.Predicate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import com.bothash.crmbot.dto.DashboardBasicResponse;
import com.bothash.crmbot.dto.DashboardCardData;
import com.bothash.crmbot.dto.FilterRequests;
import com.bothash.crmbot.dto.TransferLeadsRequest;
import com.bothash.crmbot.dto.YearLevel;
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
	
	int transferLeads(TransferLeadsRequest transferLeadsRequest);

	List<ActiveTask> getByOwnerAndActive(String role, String userName);

	List<ActiveTask> getByOwnerAndActiveAndCourseAndPlatformAndLeadType(String role, String userName, String course,
			String platform,String leadType);
	
	List<DashboardBasicResponse>  countBySpecification(FilterRequests filterRequests,Boolean isScrutiny);
	int getTodaysScheduledCount(FilterRequests filterRequests);
	int getMissedScheduledCount(FilterRequests filterRequests);
	int getCounselledCount(FilterRequests filterRequestsPassed);
	int getAdmissionCount(FilterRequests filterRequestsPassed);
	int getCountByLeadType(FilterRequests filterRequestsPassed,String leadType);
	
	DashboardCardData getDashBoardCardData(FilterRequests filterRequests);
	Map<Integer, Long> getMonthlyTaskCounts(int year,String userName,String role);
	Map<Integer, Long> getDailyTaskCounts(int year, int month,String userName,String role);
	Map<Integer, Long> getYearlyTaskCounts(String userName,String role);

	Map<String, Long> countTaskStats(Integer year, Integer month, Integer day, String course, String leadType, Set<String> roles, String userName,Boolean isActive);

	Page<ActiveTask> getFilteredTasks(Integer year, Integer month, Integer day,
			String course, String leadType, Predicate extraPredicate, int page, int size,Set<String> roles, String userName);

	int getCountNoSchedule(FilterRequests filterRequestsPassed);

	int getCountNoComment(FilterRequests filterRequestsPassed);

	int getCountTotal(FilterRequests filterRequestsPassed);

	int getCountDustbin(FilterRequests filterRequestsPassed);

	int getNotCounselledCount(FilterRequests filterRequestsPassed);

	List<Object[]> countTasksPerYearByRole(String role, String userName);

	List<Object[]> countTasksPerMonthByRole(int year,String role, String userName);

	List<Object[]> countTasksPerDayByRole(int year,int month,String role, String userName);

	List<ActiveTask> getTodaysUnderCounsellingTaskAdmin(String preferredUsername);

	List<ActiveTask> getNotTodaysUnderCounsellingTaskAdmin(String preferredUsername);

	long getTotalCount();
}
