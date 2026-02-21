package com.bothash.crmbot.controller;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.keycloak.representations.AccessToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import com.bothash.crmbot.dto.FilterRequests;
import com.bothash.crmbot.entity.ActiveTask;
import com.bothash.crmbot.service.ActiveTaskService;
import com.bothash.crmbot.service.RoleModuleAccessService;
import com.bothash.crmbot.spec.ExcelHelper;

@RestController
@RequestMapping("/lead-summary")
public class LeadsSummaryController {
	
	@Autowired
	private ActiveTaskService activeTaskService;
	
	@Autowired
	private RoleModuleAccessService roleModuleAccessService;
	
	@Autowired
	private ExcelHelper excelHelper;
	
	@GetMapping("/monthly-count")
    public ResponseEntity<Map<String, Object>> getMonthlyCount(@RequestParam int year,Principal principal) {
		
		KeycloakAuthenticationToken token = (KeycloakAuthenticationToken) principal;
		AccessToken accessToken = token.getAccount().getKeycloakSecurityContext().getToken();
		
		String userName = accessToken.getPreferredUsername();
		Set<String> roles=token.getAccount().getRoles();
		String role = roles.stream().findFirst().orElse(null);
		
		boolean isAdminLevelData = this.roleModuleAccessService.checkIfHasAdminAccess("LEAD SUMMARY", role);
		
		String roleToPass = "";
		if(roles.contains("manager")) {
			roleToPass="manager_name";
		}else if(roles.contains("telecaller")) {
			roleToPass="telecaller_name";
			
		}else if(roles.contains("counsellor")) {
			roleToPass="counsellor_name";
			
		}
		Map<Integer, Long> counts =  new HashMap<>();
		if(roles.contains("admin") || isAdminLevelData)
			counts = activeTaskService.getMonthlyTaskCounts(year,null, null);
		else {
			 counts = activeTaskService.getMonthlyTaskCounts(year,userName,roleToPass);
		}
        Map<String, Object> response = new HashMap<>();
        response.put("year", year);
        response.put("monthlyCounts", counts);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/daily-count")
    public ResponseEntity<Map<String, Object>> getDailyCount(@RequestParam int year, @RequestParam int month,Principal principal) {
    	KeycloakAuthenticationToken token = (KeycloakAuthenticationToken) principal;
		AccessToken accessToken = token.getAccount().getKeycloakSecurityContext().getToken();
		
		String userName = accessToken.getPreferredUsername();
		Set<String> roles=token.getAccount().getRoles();
		
		String role = roles.stream().findFirst().orElse(null);
		
		boolean isAdminLevelData = this.roleModuleAccessService.checkIfHasAdminAccess("LEAD SUMMARY", role);
		
		String roleToPass = "";
		if(roles.contains("manager")) {
			roleToPass="manager_name";
		}else if(roles.contains("telecaller")) {
			roleToPass="telecaller_name";
			
		}else if(roles.contains("counsellor")) {
			roleToPass="counsellor_name";
			
		}
		
    	Map<Integer, Long> counts =  new HashMap<>();
		if(roles.contains("admin")|| isAdminLevelData)
			counts = activeTaskService.getDailyTaskCounts(year, month,null,null);
		else {
			 counts = activeTaskService.getDailyTaskCounts(year, month,userName,roleToPass);
		}
        Map<String, Object> response = new HashMap<>();
        response.put("year", year);
        response.put("month", month);
        response.put("dailyCounts", counts);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/filtered-count")
    public ResponseEntity<Map<String, Object>> getFilteredTaskCount(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer day,
            @RequestParam(required = false) String course,
            @RequestParam(required = false) String leadType,Principal principal) {
    	
    	if(leadType.equals("")) {
    		leadType = null;
    	}

    	KeycloakAuthenticationToken token = (KeycloakAuthenticationToken) principal;
		AccessToken accessToken = token.getAccount().getKeycloakSecurityContext().getToken();
		
		String userName = accessToken.getPreferredUsername();
		Set<String> roles=token.getAccount().getRoles();
		String role = roles.stream().findFirst().orElse(null);
		
		boolean isAdminLevelData = this.roleModuleAccessService.checkIfHasAdminAccess("LEAD SUMMARY", role);
		
		String roleToPass = "";
		if(roles.contains("manager")) {
			roleToPass="manager_name";
		}else if(roles.contains("telecaller")) {
			roleToPass="telecaller_name";
			
		}else if(roles.contains("counsellor")) {
			roleToPass="counsellor_name";
			
		}
		if(isAdminLevelData) {
			roles=null;
		}
		Map<String, Long> counts = activeTaskService.countTaskStats(year, month, day, course, leadType,roles,userName,false);
		

        Map<String, Object> response = new HashMap<>();
        response.put("year", year);
        if (month != null) response.put("month", month);
        if (day != null) response.put("day", day);
        if (course != null) response.put("course", course);
        if (leadType != null) response.put("leadType", leadType);
        response.putAll(counts);

        return ResponseEntity.ok(response);
    }
    @GetMapping("/filterDefault")
    public Page<ActiveTask> getFilteredTasks(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer day,
            @RequestParam(required = false) String course,
            @RequestParam(required = false) String leadType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,Principal principal) {
    	if(leadType.equals("")) {
    		leadType = null;
    	}
    	KeycloakAuthenticationToken token = (KeycloakAuthenticationToken) principal;
		AccessToken accessToken = token.getAccount().getKeycloakSecurityContext().getToken();
		
		String userName = accessToken.getPreferredUsername();
		Set<String> roles=token.getAccount().getRoles();
		String role = roles.stream().findFirst().orElse(null);
		
		boolean isAdminLevelData = this.roleModuleAccessService.checkIfHasAdminAccess("LEAD SUMMARY", role);
		if(isAdminLevelData) {
			roles = null;
		}
		
        Page<ActiveTask> pagedTasks = activeTaskService.getFilteredTasks(year, month, day, course, leadType, null, page, size,roles,userName);
        
        return pagedTasks;
    }
    
    @GetMapping("/without-comments")
    public Page<ActiveTask> getTasksWithoutComments( 
            @RequestParam(required = false) String course,
            @RequestParam(required = false) String leadType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,Principal principal
    ) {
    	if(leadType.equals("")) {
    		leadType = null;
    	}
    	KeycloakAuthenticationToken token = (KeycloakAuthenticationToken) principal;
		AccessToken accessToken = token.getAccount().getKeycloakSecurityContext().getToken();
		
		String userName = accessToken.getPreferredUsername();
		Set<String> roles=token.getAccount().getRoles();
		
		String role = roles.stream().findFirst().orElse(null);
		
		boolean isAdminLevelData = this.roleModuleAccessService.checkIfHasAdminAccess("LEAD SUMMARY", role);
		
    	FilterRequests filterRequests = new FilterRequests();
    	filterRequests.setCourseName(course);
    	filterRequests.setLeadType(leadType);
    	filterRequests.setHasComments(false);
    	filterRequests.setIsActive(true);
    	filterRequests.setIsAllTask(true);
    	filterRequests.setIsLeadSummary(true);
    	if(roles.contains("admin") || isAdminLevelData) {
			
			filterRequests.setIsAdmin(true);
			filterRequests.setIsManager(false);
			filterRequests.setIsTeleCaller(false);
			filterRequests.setIsCounsellor(false);
			
			
		}else if(roles.contains("manager")) {
			filterRequests.setIsAdmin(false);
			filterRequests.setIsManager(true);
			filterRequests.setIsTeleCaller(false);
			filterRequests.setIsCounsellor(false);
			filterRequests.setUserName(userName);
		}else if(roles.contains("telecaller")) {
			filterRequests.setIsAdmin(false);
			filterRequests.setIsManager(false);
			filterRequests.setIsTeleCaller(true);
			filterRequests.setIsCounsellor(false);
			filterRequests.setUserName(userName);
		}else if(roles.contains("counsellor")) {
			filterRequests.setIsAdmin(false);
			filterRequests.setIsManager(false);
			filterRequests.setIsTeleCaller(false);
			filterRequests.setIsCounsellor(true);
			filterRequests.setUserName(userName);
		}
    	
    	Pageable requestedPage = PageRequest.of(page, size, Sort.by("createdOn").descending());
    	
        return activeTaskService.getAllTasks(filterRequests, requestedPage,true);
    }
    
    @GetMapping("/todaysSchedule")
    public Page<ActiveTask> getTodaysSchedule( 
            @RequestParam(required = false) String course,
            @RequestParam(required = false) String leadType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,Principal principal
    ) {
    	if(leadType.equals("")) {
    		leadType = null;
    	}
    	KeycloakAuthenticationToken token = (KeycloakAuthenticationToken) principal;
		AccessToken accessToken = token.getAccount().getKeycloakSecurityContext().getToken();
		
		String userName = accessToken.getPreferredUsername();
		Set<String> roles=token.getAccount().getRoles();
		
		String role = roles.stream().findFirst().orElse(null);
		
		boolean isAdminLevelData = this.roleModuleAccessService.checkIfHasAdminAccess("LEAD SUMMARY", role);
		
    	FilterRequests filterRequests = new FilterRequests();
    	filterRequests.setCourseName(course);
    	filterRequests.setLeadType(leadType);
    	filterRequests.setIsScheduled(true);
    	filterRequests.setIsAllTask(true);
    	filterRequests.setIsLeadSummary(true);
    	if(roles.contains("admin")  || isAdminLevelData) {
			
			filterRequests.setIsAdmin(true);
			filterRequests.setIsManager(false);
			filterRequests.setIsTeleCaller(false);
			filterRequests.setIsCounsellor(false);
			
			
		}else if(roles.contains("manager")) {
			filterRequests.setIsAdmin(false);
			filterRequests.setIsManager(true);
			filterRequests.setIsTeleCaller(false);
			filterRequests.setIsCounsellor(false);
			filterRequests.setUserName(userName);
		}else if(roles.contains("telecaller")) {
			filterRequests.setIsAdmin(false);
			filterRequests.setIsManager(false);
			filterRequests.setIsTeleCaller(true);
			filterRequests.setIsCounsellor(false);
			filterRequests.setUserName(userName);
		}else if(roles.contains("counsellor")) {
			filterRequests.setIsAdmin(false);
			filterRequests.setIsManager(false);
			filterRequests.setIsTeleCaller(false);
			filterRequests.setIsCounsellor(true);
			filterRequests.setUserName(userName);
		}
    	LocalDateTime dateTime = LocalDateTime.now().plusHours(5).plusMinutes(30);
    	try {
    		//LocalDate date = LocalDate.parse(scheduleTime);
    		//dateTime = date.atStartOfDay();
        	filterRequests.setScheduledTime(dateTime);
    	}catch(Exception e) {
    		e.printStackTrace();
    		//filterRequests.setScheduledTime(dateTime);
    	}
    	
    	
    	filterRequests.setIsActive(true);
    	Pageable requestedPage = PageRequest.of(page, size, Sort.by("createdOn").descending());
        return activeTaskService.getAllTasks(filterRequests, requestedPage,true);
    }
    
    @GetMapping("/withoutSchedule")
    public Page<ActiveTask> getwithoutSchedule( 
            @RequestParam(required = false) String course,
            @RequestParam(required = false) String leadType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,Principal principal
    ) {
    	if(leadType.equals("")) {
    		leadType = null;
    	}
    	KeycloakAuthenticationToken token = (KeycloakAuthenticationToken) principal;
		AccessToken accessToken = token.getAccount().getKeycloakSecurityContext().getToken();
		
		String userName = accessToken.getPreferredUsername();
		Set<String> roles=token.getAccount().getRoles();
		
		String role = roles.stream().findFirst().orElse(null);
		
		boolean isAdminLevelData = this.roleModuleAccessService.checkIfHasAdminAccess("LEAD SUMMARY", role);
    	
    	FilterRequests filterRequests = new FilterRequests();
    	filterRequests.setIsLeadSummary(true);
    	filterRequests.setCourseName(course);
    	filterRequests.setLeadType(leadType);
    	filterRequests.setIsScheduled(false);
    	filterRequests.setIsAllTask(true);
    	filterRequests.setIsLeadSummary(true);
    	if(roles.contains("admin")  || isAdminLevelData) {
			
			filterRequests.setIsAdmin(true);
			filterRequests.setIsManager(false);
			filterRequests.setIsTeleCaller(false);
			filterRequests.setIsCounsellor(false);
			
			
		}else if(roles.contains("manager")) {
			filterRequests.setIsAdmin(false);
			filterRequests.setIsManager(true);
			filterRequests.setIsTeleCaller(false);
			filterRequests.setIsCounsellor(false);
			filterRequests.setUserName(userName);
		}else if(roles.contains("telecaller")) {
			filterRequests.setIsAdmin(false);
			filterRequests.setIsManager(false);
			filterRequests.setIsTeleCaller(true);
			filterRequests.setIsCounsellor(false);
			filterRequests.setUserName(userName);
		}else if(roles.contains("counsellor")) {
			filterRequests.setIsAdmin(false);
			filterRequests.setIsManager(false);
			filterRequests.setIsTeleCaller(false);
			filterRequests.setIsCounsellor(true);
			filterRequests.setUserName(userName);
		}
    	LocalDateTime dateTime = LocalDateTime.now().plusHours(5).plusMinutes(30);
    	try {
    		//LocalDate date = LocalDate.parse(scheduleTime);
    		//dateTime = date.atStartOfDay();
        	filterRequests.setScheduledTime(dateTime);
    	}catch(Exception e) {
    		e.printStackTrace();
    		//filterRequests.setScheduledTime(dateTime);
    	}
    	filterRequests.setIsActive(true);
    	Pageable requestedPage = PageRequest.of(page, size, Sort.by("createdOn").descending());
        return activeTaskService.getAllTasks(filterRequests, requestedPage,true);
    }
    
    @GetMapping("/counsellingDone")
    public Page<ActiveTask> getCounsellingDone( 
            @RequestParam(required = false) String course,
            @RequestParam(required = false) String leadType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,Principal principal
    ) {
    	if(leadType.equals("")) {
    		leadType = null;
    	}
    	KeycloakAuthenticationToken token = (KeycloakAuthenticationToken) principal;
		AccessToken accessToken = token.getAccount().getKeycloakSecurityContext().getToken();
		
		
		String userName = accessToken.getPreferredUsername();
		Set<String> roles=token.getAccount().getRoles();
		String role = roles.stream().findFirst().orElse(null);
		
		boolean isAdminLevelData = this.roleModuleAccessService.checkIfHasAdminAccess("LEAD SUMMARY", role);
		
    	FilterRequests filterRequests = new FilterRequests();
    	filterRequests.setIsLeadSummary(true);
    	filterRequests.setCourseName(course);
    	filterRequests.setLeadType(leadType);
    	filterRequests.setIsCounselled(true);
    
    	filterRequests.setIsActive(true);
    	filterRequests.setIsAllTask(true);
    	if(roles.contains("admin")  || isAdminLevelData) {
			
			filterRequests.setIsAdmin(true);
			filterRequests.setIsManager(false);
			filterRequests.setIsTeleCaller(false);
			filterRequests.setIsCounsellor(false);
			
			
		}else if(roles.contains("manager")) {
			filterRequests.setIsAdmin(false);
			filterRequests.setIsManager(true);
			filterRequests.setIsTeleCaller(false);
			filterRequests.setIsCounsellor(false);
			filterRequests.setUserName(userName);
		}else if(roles.contains("telecaller")) {
			filterRequests.setIsAdmin(false);
			filterRequests.setIsManager(false);
			filterRequests.setIsTeleCaller(true);
			filterRequests.setIsCounsellor(false);
			filterRequests.setUserName(userName);
		}else if(roles.contains("counsellor")) {
			filterRequests.setIsAdmin(false);
			filterRequests.setIsManager(false);
			filterRequests.setIsTeleCaller(false);
			filterRequests.setIsCounsellor(true);
			filterRequests.setUserName(userName);
		}
    	Pageable requestedPage = PageRequest.of(page, size, Sort.by("createdOn").descending());
        return activeTaskService.getAllTasks(filterRequests, requestedPage,true);
    }
    
    @GetMapping("/counsellingNotDone")
    public Page<ActiveTask> getCounsellingNotDone( 
            @RequestParam(required = false) String course,
            @RequestParam(required = false) String leadType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,Principal principal
    ) {
    	if(leadType.equals("")) {
    		leadType = null;
    	}
    	KeycloakAuthenticationToken token = (KeycloakAuthenticationToken) principal;
		AccessToken accessToken = token.getAccount().getKeycloakSecurityContext().getToken();
		
		String userName = accessToken.getPreferredUsername();
		Set<String> roles=token.getAccount().getRoles();
		
		String role = roles.stream().findFirst().orElse(null);
		
		boolean isAdminLevelData = this.roleModuleAccessService.checkIfHasAdminAccess("LEAD SUMMARY", role);
		
    	FilterRequests filterRequests = new FilterRequests();
    	filterRequests.setIsLeadSummary(true);
    	filterRequests.setCourseName(course);
    	filterRequests.setLeadType(leadType);
    	filterRequests.setIsCounselled(false);
    	filterRequests.setIsActive(true);
    	filterRequests.setIsAllTask(true);
    	
    	if(roles.contains("admin")  || isAdminLevelData) {
			
			filterRequests.setIsAdmin(true);
			filterRequests.setIsManager(false);
			filterRequests.setIsTeleCaller(false);
			filterRequests.setIsCounsellor(false);
			
			
		}else if(roles.contains("manager")) {
			filterRequests.setIsAdmin(false);
			filterRequests.setIsManager(true);
			filterRequests.setIsTeleCaller(false);
			filterRequests.setIsCounsellor(false);
			filterRequests.setUserName(userName);
		}else if(roles.contains("telecaller")) {
			filterRequests.setIsAdmin(false);
			filterRequests.setIsManager(false);
			filterRequests.setIsTeleCaller(true);
			filterRequests.setIsCounsellor(false);
			filterRequests.setUserName(userName);
		}else if(roles.contains("counsellor")) {
			filterRequests.setIsAdmin(false);
			filterRequests.setIsManager(false);
			filterRequests.setIsTeleCaller(false);
			filterRequests.setIsCounsellor(true);
			filterRequests.setUserName(userName);
		}
    	Pageable requestedPage = PageRequest.of(page, size, Sort.by("createdOn").descending());
        return activeTaskService.getAllTasks(filterRequests, requestedPage,true);
    }
    
    @GetMapping("/tab-count")
    public ResponseEntity<Map<String,Integer>> tabCount(@RequestParam(required = false) String course,
            @RequestParam(required = false) String leadType,@RequestParam(required = false) String year,@RequestParam(required = false) String month,@RequestParam(required = false) String day,Principal principal){
    	int yearToPass=0,monthToPass=0,dayToPass=0;
    	if(year!=null && year!="") {
    		yearToPass =  Integer.parseInt(year);
    	}
    	if(month!=null && month!="") {
    		monthToPass =  Integer.parseInt(month);
    	}
    	if(day!=null && day!="") {
    		dayToPass =  Integer.parseInt(day);
    	}
  
    		
    	KeycloakAuthenticationToken token = (KeycloakAuthenticationToken) principal;
		AccessToken accessToken = token.getAccount().getKeycloakSecurityContext().getToken();
		
		String userName = accessToken.getPreferredUsername();
		Set<String> roles=token.getAccount().getRoles();
		
		String role = roles.stream().findFirst().orElse(null);
		
		boolean isAdminLevelData = this.roleModuleAccessService.checkIfHasAdminAccess("LEAD SUMMARY", role);
		
		
    	FilterRequests filterRequests = new FilterRequests();
    	filterRequests.setCourseName(course);
    	filterRequests.setIsLeadSummary(true);
    	filterRequests.setIsActive(true);
    	if(leadType.equals("")) {
    		leadType = null;
    	}
    	filterRequests.setLeadType(leadType);
    	
    	if(roles.contains("admin") || isAdminLevelData) {
			
			filterRequests.setIsAdmin(true);
			filterRequests.setIsManager(false);
			filterRequests.setIsTeleCaller(false);
			filterRequests.setIsCounsellor(false);
			
			
		}else if(roles.contains("manager")) {
			filterRequests.setIsAdmin(false);
			filterRequests.setIsManager(true);
			filterRequests.setIsTeleCaller(false);
			filterRequests.setIsCounsellor(false);
			filterRequests.setUserName(userName);
		}else if(roles.contains("telecaller")) {
			filterRequests.setIsAdmin(false);
			filterRequests.setIsManager(false);
			filterRequests.setIsTeleCaller(true);
			filterRequests.setIsCounsellor(false);
			filterRequests.setUserName(userName);
		}else if(roles.contains("counsellor")) {
			filterRequests.setIsAdmin(false);
			filterRequests.setIsManager(false);
			filterRequests.setIsTeleCaller(false);
			filterRequests.setIsCounsellor(true);
			filterRequests.setUserName(userName);
		}
    	
    	
    	int todaysScheduledCount = activeTaskService.getTodaysScheduledCount(filterRequests);
    	int totalLeadCount = activeTaskService.getCountTotal(filterRequests);
    	int notScheduledCount = activeTaskService.getCountNoSchedule(filterRequests);
    	int noCommentCount = activeTaskService.getCountNoComment(filterRequests);
    	int counselledCount = activeTaskService.getCounselledCount(filterRequests);
    	int notCounselledCount = activeTaskService.getNotCounselledCount(filterRequests);
    	Map<String, Long> counts = null;
    	if(isAdminLevelData) {
    		counts = activeTaskService.countTaskStats(yearToPass==0?null:yearToPass, monthToPass==0?null:monthToPass, dayToPass==0?null:dayToPass, course, leadType,null,userName,true);
    	}else {
    		
    		counts = activeTaskService.countTaskStats(yearToPass==0?null:yearToPass, monthToPass==0?null:monthToPass, dayToPass==0?null:dayToPass, course, leadType,roles,userName,true);
    	}
    	
    	if(counts!=null) {
    		totalLeadCount = (counts.get("totalTasks")).intValue();
    	}
    	
    	Map<String,Integer> response = new HashMap<>();
    	response.put("totalLeadCount", totalLeadCount);
    	response.put("todaysScheduledCount", todaysScheduledCount);
    	response.put("notScheduledCount", notScheduledCount);
    	response.put("noCommentCount", noCommentCount);
    	response.put("counselledCount", counselledCount);
    	response.put("notCounselledCount", notCounselledCount);
    	return new ResponseEntity<Map<String,Integer>>(response,HttpStatus.OK);
    }

    @GetMapping("/taskDetails/{taskId}")
    public ModelAndView taskDetails(@PathVariable Long taskId) {
    	ModelAndView model = new ModelAndView();
    	ActiveTask activeTask = this.activeTaskService.getTaskById(taskId);
    	model.addObject("activeTask", activeTask);
    	model.setViewName("taskDetailsLeadSummary");
        return model;
    }
    
    @PostMapping("/report")
    public Callable<ResponseEntity<Resource>> report(
    		 @RequestParam(required = false) Integer year,
             @RequestParam(required = false) Integer month,
             @RequestParam(required = false) Integer day,
             @RequestParam(required = false) String course,
             @RequestParam(required = false) String leadType,
             @RequestParam(required = false) String tabName,
             Principal principal){
    	
    	KeycloakAuthenticationToken token = (KeycloakAuthenticationToken) principal;
		AccessToken accessToken = token.getAccount().getKeycloakSecurityContext().getToken();
		
		long totalCount = this.activeTaskService.getTotalCount();
		Page<ActiveTask> activeTaskPage = null;
		if(tabName.equalsIgnoreCase("filterDefault")) {
			activeTaskPage = this.getFilteredTasks(year, month, day, course, leadType, 0, (int)totalCount, principal);
		}else if(tabName.equalsIgnoreCase("without-comments")) {
			activeTaskPage = this.getTasksWithoutComments(course, leadType, 0, (int)totalCount, principal);
		}else if(tabName.equalsIgnoreCase("todaysSchedule")) {
			activeTaskPage = this.getTodaysSchedule(course, leadType, 0, (int)totalCount, principal);
		}else if(tabName.equalsIgnoreCase("withoutSchedule")) {
			activeTaskPage = this.getwithoutSchedule(course, leadType, 0, (int)totalCount, principal);
		}else if(tabName.equalsIgnoreCase("counsellingNotDone")) {
			activeTaskPage = this.getCounsellingNotDone(course, leadType, 0, (int)totalCount, principal);
		}else if(tabName.equalsIgnoreCase("counsellingDone")) {
			activeTaskPage = this.getCounsellingDone(course, leadType, 0, (int)totalCount, principal);
		}
		if(activeTaskPage!=null) {
			try {
				String fileName="test.xlsx";
				ByteArrayInputStream in=excelHelper.taskToExcel(activeTaskPage.getContent());
				return () -> {
					return ResponseEntity.ok().contentType(MediaType.parseMediaType("application/octet-stream"))
						.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
						.body(new InputStreamResource(in));
				};
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
    	return null;
    }

}
