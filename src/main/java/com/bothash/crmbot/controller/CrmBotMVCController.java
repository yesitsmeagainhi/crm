package com.bothash.crmbot.controller;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import org.json.JSONArray;
import org.json.JSONObject;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.AccessToken;
import org.keycloak.representations.idm.RoleRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.ModelAndView;

import com.bothash.crmbot.dto.Constants;
import com.bothash.crmbot.dto.FilterRequests;
import com.bothash.crmbot.dto.Groups;
import com.bothash.crmbot.dto.YearLevel;
import com.bothash.crmbot.entity.ActiveTask;
import com.bothash.crmbot.entity.Automation;
import com.bothash.crmbot.entity.AutomationUsers;
import com.bothash.crmbot.entity.CloseTask;
import com.bothash.crmbot.entity.CommentMaster;
import com.bothash.crmbot.entity.Comments;
import com.bothash.crmbot.entity.CounsellingDetails;
import com.bothash.crmbot.entity.Course;
import com.bothash.crmbot.entity.FacebookLeadConfigs;
import com.bothash.crmbot.entity.HistoryEvents;
import com.bothash.crmbot.entity.Message;
import com.bothash.crmbot.entity.Platforms;
import com.bothash.crmbot.entity.RoleModuleAccess;
import com.bothash.crmbot.entity.UserMaster;
import com.bothash.crmbot.service.ActiveTaskService;
import com.bothash.crmbot.service.AutomationService;
import com.bothash.crmbot.service.AutomationUserService;
import com.bothash.crmbot.service.CloseTaskService;
import com.bothash.crmbot.service.CommentMasterService;
import com.bothash.crmbot.service.CommentsService;
import com.bothash.crmbot.service.CounsellingDetailsService;
import com.bothash.crmbot.service.CourseService;
import com.bothash.crmbot.service.FacebookLeadConfigService;
import com.bothash.crmbot.service.HistoryEventsService;
import com.bothash.crmbot.service.PlatformService;
import com.bothash.crmbot.service.RoleModuleAccessService;
import com.bothash.crmbot.service.UserMasterService;
import com.bothash.crmbot.service.impl.MessageService;
import com.bothash.crmbot.spec.ExcelHelper;
import com.bothash.crmbot.spec.FilterSpecification;

import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/crmbot/")
@Slf4j
public class CrmBotMVCController {

	@Autowired
	private ActiveTaskService activeTaskService;

	@Autowired
	private CommentsService commentsService;

	@Autowired
	private HistoryEventsService historyEventsService;

	@Autowired
	private CloseTaskService closeTaskService;

	@Autowired
	private CounsellingDetailsService conCounsellingDetailsService;

	@Autowired
	private ExcelHelper excelHelper;

	@Autowired
	private CourseService courseService;

	@Autowired
	private PlatformService platfromService;

	@Autowired
	private FacebookLeadConfigService facebookLeadConfigService;

	@Autowired
	private AutomationService automationService;

	@Autowired
	private AutomationUserService automationUserService;

	@Autowired
	private UserMasterService userMasterService;

	@Autowired
	private MessageService messageService;

	@Autowired
	private RoleModuleAccessService roleModuleAccessService;

	@Autowired
	private CommentMasterService commentMasterService;

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private Keycloak keycloak;

	@Value("${keycloak.auth-server-url}")
	private String keycloackUrl;

	@Value("${keycloack.admin.username}")
	private String adminUserName;

	@Value("${keycloack.admin.password}")
	private String adminPassword;

	@Value("${keycloak.credentials.secret}")
	private String keycloackClientSecret;

	@Value("${crmbot-client-id}")
	private String crmbotClientId;

	@Value("${keycloak.realm}")
	private String keycloackRealm;

//	
//	
//	private  void updateUserEmails(String accessToken) {
//        String usersUrl = this.keycloackUrl + "/admin/realms/" + this.keycloackRealm + "/users";
//        RestTemplate restTemplate = new RestTemplate();
//
//        // Set the Authorization header with the access token
//        HttpHeaders headers = new HttpHeaders();
//        headers.set("Authorization", "Bearer " + accessToken);
//        headers.setContentType(MediaType.APPLICATION_JSON);
//
//        HttpEntity<String> entity = new HttpEntity<>(headers);
//
//        // Fetch all users
//        ResponseEntity<String> response = restTemplate.exchange(usersUrl, HttpMethod.GET, entity, String.class);
//
//        if (response.getStatusCode() == HttpStatus.OK) {
//            JSONArray usersJson = new JSONArray(response.getBody());
//
//            // Loop through all users and update their email
//            for (int i=0;i<usersJson.length();i++) {
//            	JSONObject user = usersJson.getJSONObject(i);
//                String userId = user.get("id").toString();
//                String username = user.get("username").toString();
//                String email = username; // Set email to match username
//
//                updateEmailForUser(userId, email, accessToken);
//            }
//        } else {
//            System.out.println("Failed to fetch users.");
//        }
//    }
//	private  void updateEmailForUser(String userId, String email, String accessToken) {
//        String updateUserUrl = this.keycloackUrl + "/admin/realms/" + this.keycloackRealm + "/users/" + userId;
//        RestTemplate restTemplate = new RestTemplate();
//
//        // Prepare the request body for the email update
//        String jsonBody = "{ \"email\": \"" + email + "\", \"emailVerified\": true }";
//        HttpHeaders headers = new HttpHeaders();
//        headers.set("Authorization", "Bearer " + accessToken);
//        headers.setContentType(MediaType.APPLICATION_JSON);
//
//        HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);
//
//        // Update the email
//        ResponseEntity<String> response = restTemplate.exchange(updateUserUrl, HttpMethod.PUT, entity, String.class);
//
//        if (response.getStatusCode() == HttpStatus.NO_CONTENT) {
//            System.out.println("Successfully updated email for user " + userId);
//        } else {
//            System.out.println("Failed to update email for user " + userId);
//        }
//    }
	@GetMapping("tasks")
	public ModelAndView taks(Principal principal) {
		KeycloakAuthenticationToken token = (KeycloakAuthenticationToken) principal;
		AccessToken accessToken = token.getAccount().getKeycloakSecurityContext().getToken();

		ModelAndView model = new ModelAndView();
		model.setViewName("tasks");

		Boolean isAdmin = false;
		Boolean isManager = false;
		Boolean isTelecaller = false;
		Boolean isCounsellor = false;
		Boolean isSupervisor = false;

		Set<String> roles = token.getAccount().getRoles();
		String role = roles.stream().findFirst().orElse(null);

		String userName = accessToken.getPreferredUsername();
		Boolean isAutomated = false;
		if (role.equalsIgnoreCase("admin")) {
			isAdmin = true;
			List<Automation> autoamtions = this.automationService.getByIsActive(true);
			if (autoamtions.size() > 0) {
				isAutomated = true;
			}
			role = "admin";
		} else if (role.equalsIgnoreCase("manager")) {

			AutomationUsers automationUser = automationUserService.getByUserId(userName);
			if (automationUser != null) {
				isAutomated = true;
			}
			role = "manager";
			isManager = true;
		} else if (role.equalsIgnoreCase("telecaller")) {
			role = "telecaller";
			isTelecaller = true;
		} else if (role.equalsIgnoreCase("counsellor")) {
			role = "counsellor";
			isCounsellor = true;
		}
		if (role.equalsIgnoreCase("supervisor")) {
			role = "supervisor";
			isSupervisor = true;
		}
		model.addObject("isAutomated", isAutomated);
		model.addObject("role", role);
		model.addObject("userName", accessToken.getName());
		Pageable requestedPage = PageRequest.of(0, 15, Sort.by("createdOn").descending());

		Page<ActiveTask> myTasks = this.activeTaskService.getMyTask(role, userName, requestedPage);
		Page<CloseTask> completedTasks = this.closeTaskService.convertedTask(role, userName, requestedPage);
		if (isAdmin) {
			Page<ActiveTask> allActiveTask = this.activeTaskService.getAllTasks(requestedPage);
			model.addObject("allActiveTask", allActiveTask);
		} else if (role.equalsIgnoreCase("manager")) {
			Page<ActiveTask> allActiveTask = this.activeTaskService.getManagerTask(userName, requestedPage);
			model.addObject("allActiveTask", allActiveTask);
		} else if (role.equalsIgnoreCase("telecaller")) {
			Page<ActiveTask> allActiveTask = this.activeTaskService.getTeleCallerTask(userName, requestedPage);
			model.addObject("allActiveTask", allActiveTask);
		}

		UserMaster userDetails = this.userMasterService.getByUserName(userName);
		if (userDetails != null)
			model.addObject("isUserActive", userDetails.getIsActive());
		else
			model.addObject("isUserActive", true);

		model.addObject("prefferedUserName", userName);

		List<Platforms> platforms = platfromService.getAll();
		model.addObject("platforms", platforms);

		model.addObject("isAdmin", isAdmin);
		model.addObject("isManager", isManager);
		model.addObject("isTelecaller", isTelecaller);
		model.addObject("isCounsellor", isCounsellor);
		model.addObject("isSupervisor", isSupervisor);
		model.addObject("myTasks", myTasks);
		model.addObject("completedTasks", completedTasks);
		model.addObject("task", true);

		List<String> nextRoles = new ArrayList<>();
		nextRoles.add("manager");
		nextRoles.add("telecaller");
		nextRoles.add("counsellor");

		HttpHeaders header = new HttpHeaders();
		MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
		body.add("grant_type", "password");
		body.add("client_secret", keycloackClientSecret);
		body.add("username", adminUserName);
		body.add("password", adminPassword);
		body.add("client_id", "admin-cli");

		HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(body, header);

		@SuppressWarnings("rawtypes")
		HashMap response = restTemplate.postForObject(keycloackUrl + "/realms/master/protocol/openid-connect/token",
				entity, HashMap.class);
		String adminAccessToken = response.get("access_token").toString();

//		this.updateUserEmails(adminAccessToken);

		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.set("Authorization", "Bearer " + adminAccessToken);

		List<Object> nextUsers = new ArrayList<>();

		for (String nextRole : nextRoles) {
			ResponseEntity<Object> userResponse = restTemplate.exchange(
					keycloackUrl + "/admin/realms/crmbot/clients/" + crmbotClientId + "/roles/" + nextRole + "/users",
					HttpMethod.GET, new HttpEntity<>(httpHeaders), Object.class);
			try {
				List<LinkedHashMap<String, Object>> userList = (List<LinkedHashMap<String, Object>>) userResponse
						.getBody();
				for (LinkedHashMap<String, Object> userMap : userList) {
					try {
						UserMaster user = this.userMasterService.getByUserName(userMap.get("username").toString());
						if (user != null) {
							userMap.put("isActiveOnCRM", user.getIsActive());
						}
					} catch (Exception e) {
						e.printStackTrace();
						userMap.put("isActiveOnCRM", false);
					}

				}
				nextUsers.add(userResponse.getBody());
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
		model.addObject("nextUsers", nextUsers);
		model.addObject("nextRoles", nextRoles);

		List<Course> courses = this.courseService.getAll();
		model.addObject("courses", courses);

		List<RoleModuleAccess> access = this.roleModuleAccessService.getByRole(role);
		model.addObject("access", access);

		return model;
	}

	@ResponseBody
	@PutMapping("singletasktable")
	public ModelAndView singletasktable(@RequestParam(value = "page") int page, @RequestParam(value = "size") int size,
			@RequestParam(value = "sorting") String sorting, @RequestParam(value = "desc") boolean desc,
			@RequestParam(value = "taskType") String taskType,
			@RequestParam(value = "isFilter", required = false) Boolean isFilter,
			@RequestBody(required = false) FilterRequests filterRequests, Principal principal) {
		ModelAndView model = new ModelAndView();
		model.addObject("isScheduled", false);
		System.out.println("SINGLETASK TABLE CALLED");
		if (isFilter == null) {

			isFilter = false;
		} else if (isFilter) {
			if (taskType.equalsIgnoreCase(Constants.myTask)) {
				filterRequests.setIsOwner(true);
			} else {
				filterRequests.setIsOwner(false);
			}
			filterRequests.setStatus("");
		}

		Pageable requestedPage = PageRequest.of(page, size, desc ? Sort.by(sorting).descending() : Sort.by(sorting));

		@SuppressWarnings("unused")
		Boolean isAdmin = false;
		@SuppressWarnings("unused")
		Boolean isClosedTask = false;
		Boolean isSearchLead = false;

		KeycloakAuthenticationToken token = (KeycloakAuthenticationToken) principal;
		AccessToken accessToken = token.getAccount().getKeycloakSecurityContext().getToken();
		filterRequests.setIsActive(true);
		Set<String> roles = token.getAccount().getRoles();
		String role = roles.stream().findFirst().orElse(null);
		
		boolean isAdminLevelData = this.roleModuleAccessService.checkIfHasAdminAccess("TASKS", role);
		
		if (isAdminLevelData) {
			isAdmin = true;
			role = "admin";
			filterRequests.setIsAdmin(true);
			filterRequests.setIsManager(false);
			filterRequests.setIsTeleCaller(false);
			filterRequests.setIsCounsellor(false);

		} else if (roles.contains("manager")) {
			role = "manager";
			filterRequests.setIsAdmin(false);
			filterRequests.setIsManager(true);
			filterRequests.setIsTeleCaller(false);
			filterRequests.setIsCounsellor(false);
		} else if (roles.contains("telecaller")) {
			role = "telecaller";
			filterRequests.setIsAdmin(false);
			filterRequests.setIsManager(false);
			filterRequests.setIsTeleCaller(true);
			filterRequests.setIsCounsellor(false);
		} else if (roles.contains("counsellor")) {
			role = "counsellor";
			filterRequests.setIsAdmin(false);
			filterRequests.setIsManager(false);
			filterRequests.setIsTeleCaller(false);
			filterRequests.setIsCounsellor(true);
		}
		Page<ActiveTask> tasks = null;
		Page<CloseTask> completedtasks = null;
		if (taskType.equals(Constants.searchLead)) {
			filterRequests.setIsAdmin(true);
			filterRequests.setIsManager(false);
			filterRequests.setIsTeleCaller(false);
			filterRequests.setIsCounsellor(false);
			filterRequests.setIsAllTask(true);
			tasks = this.activeTaskService.getAllTasks(filterRequests, requestedPage, isFilter);
			model.addObject("allTask", tasks);
			isSearchLead = true;

		} else if (taskType.equals(Constants.myTask)) {
			filterRequests.setIsMyTask(true);
			filterRequests.setRole(role);
			if (isAdminLevelData)
				filterRequests.setIsAdmin(true);
			else
				filterRequests.setIsAdmin(false);
			if (isFilter == null || !isFilter) {

				filterRequests.setUserName(accessToken.getPreferredUsername());
			}
			tasks = this.activeTaskService.getAllTasks(filterRequests, requestedPage, isFilter);
			model.addObject("myTask", tasks);
		}

		else if (taskType.equalsIgnoreCase(Constants.allTask)) {
			filterRequests.setIsAllTask(true);
			filterRequests.setRole(role);

			if (isFilter == null || !isFilter) {

				filterRequests.setUserName(accessToken.getPreferredUsername());
			}

			if (isAdmin) {

				tasks = this.activeTaskService.getAllTasks(filterRequests, requestedPage, isFilter);
				model.addObject("allTask", tasks);
			} else if (role.equalsIgnoreCase("manager")) {

				tasks = this.activeTaskService.getAllTasks(filterRequests, requestedPage, isFilter);
				model.addObject("allTask", tasks);
			} else if (role.equalsIgnoreCase("telecaller")) {

				tasks = this.activeTaskService.getAllTasks(filterRequests, requestedPage, isFilter);
				model.addObject("allTask", tasks);
			} else if (role.equalsIgnoreCase("counsellor")) {

				tasks = this.activeTaskService.getAllTasks(filterRequests, requestedPage, isFilter);
				model.addObject("allTask", tasks);
			}

		} else if (taskType.equalsIgnoreCase(Constants.counselledTask)) {
			filterRequests.setIsAllTask(true);
			filterRequests.setRole(role);
			if (isFilter == null || !isFilter) {

				filterRequests.setUserName(accessToken.getPreferredUsername());
			}
			filterRequests.setIsCounselled(true);
			requestedPage = PageRequest.of(page, size, Sort.by("modifiedOn").descending());
			tasks = this.activeTaskService.getAllTasks(filterRequests, requestedPage, isFilter);
			model.addObject("counselledTask", tasks);
		} else if (taskType.equalsIgnoreCase(Constants.shceduledTask)) {
			requestedPage = PageRequest.of(page, size, Sort.by("scheduleTime"));

			if (isAdminLevelData) {
				tasks = this.activeTaskService.getTodaysScheduledTaskForAdmin(requestedPage);
				if (size - tasks.getNumberOfElements() > 0) {
					Pageable requestedPage2 = PageRequest.of(page, size - tasks.getNumberOfElements(),
							Sort.by("scheduleTime").descending());
					Page<ActiveTask> tasks2 = this.activeTaskService.getNotTodaysScheduledTaskForAdmin(requestedPage2);
					model.addObject("notTodaysshceduledTask", tasks2);
					model.addObject("isScheduled", true);
				}

			} else if (role != "manager") {
				tasks = this.activeTaskService.getTodaysScheduledTaskByOwner(requestedPage,
						accessToken.getPreferredUsername());
				if (size - tasks.getNumberOfElements() > 0) {
					Pageable requestedPage2 = PageRequest.of(page, size - tasks.getNumberOfElements(),
							Sort.by("scheduleTime").descending());
					Page<ActiveTask> tasks2 = this.activeTaskService.getNotTodaysScheduledTaskByOwner(requestedPage2,
							accessToken.getPreferredUsername());
					model.addObject("notTodaysshceduledTask", tasks2);
					model.addObject("isScheduled", true);
				}

			} else {
				tasks = this.activeTaskService.getTodaysScheduledTaskByManager(requestedPage,
						accessToken.getPreferredUsername());
				if (size - tasks.getNumberOfElements() > 0) {
					Pageable requestedPage2 = PageRequest.of(page, size - tasks.getNumberOfElements(),
							Sort.by("scheduleTime").descending());
					Page<ActiveTask> tasks2 = this.activeTaskService.getNotTodaysScheduledTaskByManager(requestedPage2,
							accessToken.getPreferredUsername());
					model.addObject("notTodaysshceduledTask", tasks2);
					model.addObject("isScheduled", true);
				}

			}
			model.addObject("allTask", tasks);
			model.addObject("shceduledTask", tasks);
		} else if (taskType.equalsIgnoreCase(Constants.meetingTask)) {
			requestedPage = PageRequest.of(page, size, Sort.by(sorting));
			if (role.equals("manager")) {
				tasks = this.activeTaskService.getTodaysUnderCounsellingTaskByManager(requestedPage,
						accessToken.getPreferredUsername());
				model.addObject("allTask", tasks);
				model.addObject("meetingTask", tasks);
//				if(size-tasks.getNumberOfElements()>0) {
//					Pageable requestedPage2 = PageRequest.of(page, size-tasks.getNumberOfElements(), Sort.by("scheduleTime"));
//					Page<ActiveTask> tasks2=this.activeTaskService.getNotTodaysUnderCounsellingTaskByManager(requestedPage2,accessToken.getPreferredUsername());
//					model.addObject("notTodaysshceduledTask",tasks2);
//					model.addObject("isScheduled",true);
//					
//				}

			} else if (isAdminLevelData) {
				tasks = this.activeTaskService.getTodaysUnderCounsellingTaskAdmin(requestedPage,
						accessToken.getPreferredUsername());
				model.addObject("allTask", tasks);
				model.addObject("meetingTask", tasks);
//				if(size-tasks.getNumberOfElements()>0) {
//					Pageable requestedPage2 = PageRequest.of(page, size-tasks.getNumberOfElements(), Sort.by("scheduleTime"));
//					Page<ActiveTask> tasks2=this.activeTaskService.getNotTodaysUnderCounsellingTaskAdmin(requestedPage2,accessToken.getPreferredUsername());
//					model.addObject("notTodaysshceduledTask",tasks2);
//					model.addObject("isScheduled",true);
//					
//				}

			}

			// else
			// tasks=this.activeTaskService.getUnderCounselling(requestedPage);

		} else {
			if (!(sorting.equalsIgnoreCase("id") || sorting.equalsIgnoreCase("createdOn"))) {
				sorting = sorting.toUpperCase().charAt(0) + sorting.substring(1);
				requestedPage = PageRequest.of(page, size,
						desc ? Sort.by("activeTask" + sorting).descending() : Sort.by("activeTask" + sorting));
			}
			if (sorting.equalsIgnoreCase("createdOnDuplicate")) {
				sorting = "createdOn";
			}
			Pageable requestedPageClosed = PageRequest.of(page, size,
					desc ? Sort.by(sorting).descending() : Sort.by(sorting));
			filterRequests.setIsActive(false);
			isClosedTask = true;
			completedtasks = this.closeTaskService.convertedTask(filterRequests, role,
					accessToken.getPreferredUsername(), requestedPageClosed,isAdminLevelData);
			model.addObject("completedtasks", completedtasks);
		}
		model.addObject("prefferedUserName", accessToken.getPreferredUsername());
		UserMaster userDetails = this.userMasterService.getByUserName(accessToken.getPreferredUsername());
		if (userDetails != null)
			model.addObject("isUserActive", userDetails.getIsActive());
		else
			model.addObject("isUserActive", true);
		model.addObject("role", role);
		model.addObject("userName", accessToken.getName());
		model.addObject("tasks", tasks);
		model.setViewName("tasksingletable");
		model.addObject("isClosedTask", isClosedTask);
		model.addObject("isSearchLead", isSearchLead);
		model.addObject("taskType", taskType);
		model.addObject("pagesize", size);

		List<RoleModuleAccess> access = this.roleModuleAccessService.getByRole(role);
		model.addObject("access", access);
		return model;
	}

	@ResponseBody
	@GetMapping("detailpage")
	public ModelAndView crmbot(@RequestParam Long id, @RequestParam String role,
			@RequestParam(required = false) String isClosed, @RequestParam(required = false) String isSearchLead,
			Principal principal) {

		ActiveTask task = this.activeTaskService.getTaskById(id);

		CloseTask closeTask = this.closeTaskService.getByActiveTask(id);

		List<Comments> comments = this.commentsService.getByActiveTask(id);

		List<CommentMaster> commentMasters = this.commentMasterService.getAllComments();

		List<CounsellingDetails> counsellingDetails = this.conCounsellingDetailsService.getByActiveTask(id);

		List<HistoryEvents> historyEvents = this.historyEventsService.getByTask(id);

		KeycloakAuthenticationToken token = (KeycloakAuthenticationToken) principal;
		AccessToken accessToken = token.getAccount().getKeycloakSecurityContext().getToken();

		ModelAndView model = new ModelAndView();
		model.addObject("comments", comments);
		model.addObject("commentMasters", commentMasters);
		model.addObject("counsellingDetails", counsellingDetails);
		model.addObject("historyEvents", historyEvents);
		model.setViewName("task-detail-page");
		model.addObject("task", task);
		model.addObject("userEmail", accessToken.getPreferredUsername());
		model.addObject("userName", accessToken.getName());
		model.addObject("userId", accessToken.getSubject());
		model.addObject("taskId", id);
		model.addObject("role", role);
		model.addObject("userName", accessToken.getName());
		try {
			JSONArray fieldData = new JSONArray(task.getFacebookLeads().getFieldData());
			model.addObject("fieldDataList", fieldData);
			log.info(fieldData + "");
		} catch (Exception e) {
			e.printStackTrace();
			log.info("unable to find field data");
		}

		List<String> nextRoles = new ArrayList<>();
		if (role.equalsIgnoreCase(Constants.admin) || role.equalsIgnoreCase(Constants.supervisor)) {
			nextRoles.add("manager");
			nextRoles.add("telecaller");
			nextRoles.add("counsellor");
		} else if (role.equalsIgnoreCase(Constants.manager)) {

			if (task.getAssignee() != null && task.getAssignee().equalsIgnoreCase("manager")
					&& (isSearchLead == null || isSearchLead.equalsIgnoreCase("false"))) {
				task.setOwner(accessToken.getPreferredUsername());
			}
			nextRoles.add("telecaller");
			nextRoles.add("counsellor");
		} else if (role.equalsIgnoreCase(Constants.telecaller)) {

			if (task.getAssignee() != null && task.getAssignee().equalsIgnoreCase("telecaller")
					&& (isSearchLead == null || isSearchLead.equalsIgnoreCase("false"))) {
				task.setOwner(accessToken.getPreferredUsername());
			}
			nextRoles.add("manager");
			nextRoles.add("counsellor");
		} else if (role.equalsIgnoreCase(Constants.counsellor)) {

			if (task.getAssignee() != null && task.getAssignee().equalsIgnoreCase("counsellor")
					&& (isSearchLead == null || isSearchLead.equalsIgnoreCase("false"))) {
				task.setOwner(accessToken.getPreferredUsername());
			}
			nextRoles.add("telecaller");
			nextRoles.add("manager");
		}

		if (isSearchLead == null || isSearchLead.equalsIgnoreCase("false")) {
			this.activeTaskService.save(task);
		}

		List<Object> nextUsers = new ArrayList<>();
		List<Object> counsellors = new ArrayList<>();
		try {
			HttpHeaders header = new HttpHeaders();
			MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
			body.add("grant_type", "password");
			body.add("client_secret", keycloackClientSecret);
			body.add("username", adminUserName);
			body.add("password", adminPassword);
			body.add("client_id", "admin-cli");

			HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(body, header);

			@SuppressWarnings("rawtypes")
			HashMap response = restTemplate.postForObject(keycloackUrl + "/realms/master/protocol/openid-connect/token",
					entity, HashMap.class);
			log.info(response + " TOLEN");
			String adminAccessToken = response.get("access_token").toString();

			HttpHeaders httpHeaders = new HttpHeaders();
			httpHeaders.set("Authorization", "Bearer " + adminAccessToken);

			for (String nextRole : nextRoles) {
				try {
					ResponseEntity<Object> userResponse = restTemplate.exchange(
							keycloackUrl + "/admin/realms/crmbot/clients/" + crmbotClientId + "/roles/" + nextRole + "/users",
							HttpMethod.GET, new HttpEntity<>(httpHeaders), Object.class);
					List<LinkedHashMap<String, Object>> userList = (List<LinkedHashMap<String, Object>>) userResponse
							.getBody();
					if (userList != null) {
						for (LinkedHashMap<String, Object> userMap : userList) {
							try {
								UserMaster user = this.userMasterService.getByUserName(userMap.get("username").toString());
								if (user != null) {
									userMap.put("isActiveOnCRM", user.getIsActive());
								}
							} catch (Exception e) {
								e.printStackTrace();
								userMap.put("isActiveOnCRM", false);
							}
						}
					}
					nextUsers.add(userResponse.getBody());
				} catch (Exception e) {
					log.warn("Failed to fetch users for role: " + nextRole, e);
				}
			}

			try {
				ResponseEntity<Object> userResponse = restTemplate.exchange(
						keycloackUrl + "/admin/realms/crmbot/clients/" + crmbotClientId + "/roles/" + "counsellor" + "/users",
						HttpMethod.GET, new HttpEntity<>(httpHeaders), Object.class);
				counsellors.add(userResponse.getBody());
			} catch (Exception e) {
				log.warn("Failed to fetch counsellors", e);
			}
		} catch (Exception e) {
			log.warn("Failed to connect to Keycloak admin API for user lists", e);
		}
		model.addObject("counsellors", counsellors);
		UserMaster userDetails = this.userMasterService.getByUserName(accessToken.getPreferredUsername());
		if (userDetails != null)
			model.addObject("isUserActive", userDetails.getIsActive());
		else
			model.addObject("isUserActive", true);

		List<Course> courses = this.courseService.getAll();

		model.addObject("courses", courses);

		model.addObject("prefferedUserName", accessToken.getPreferredUsername());
		model.addObject("nextRoles", nextRoles);
		model.addObject("nextUsers", nextUsers);
		model.addObject("isClosed", isClosed);
		model.addObject("isSearchLead", isSearchLead);
		model.addObject("closeTask", closeTask);

		List<Message> messages = this.messageService.findAll();
		model.addObject("messages", messages);

		List<RoleModuleAccess> access = this.roleModuleAccessService.getByRole(role);
		model.addObject("access", access);
		
		boolean hasEditAccess = this.roleModuleAccessService.checkIfHasAccess("COMPLETED TASK","EDIT",role);
		model.addObject("hasEditAccess", hasEditAccess);
		return model;
	}

	@GetMapping("add-task")
	public ModelAndView addTask(Principal principal) {
		ModelAndView model = new ModelAndView();
		model.addObject("addtask", true);
		model.setViewName("add-task");

		KeycloakAuthenticationToken token = (KeycloakAuthenticationToken) principal;
		AccessToken accessToken = token.getAccount().getKeycloakSecurityContext().getToken();
		String role = "";
		Boolean isAdmin = false;

		Set<String> roles = token.getAccount().getRoles();
		Boolean isAutomated = false;
		List<String> nextRoles = new ArrayList<>();
		if (roles.contains("admin") || role.contains(Constants.supervisor)) {
			isAdmin = true;
			role = "admin";
			nextRoles.add("manager");
			nextRoles.add("telecaller");
			nextRoles.add("counsellor");

			List<Automation> autoamtions = this.automationService.getByIsActive(true);
			if (autoamtions.size() > 0) {
				isAutomated = true;
			}
		} else if (roles.contains("manager")) {
			role = "manger";

			nextRoles.add("telecaller");
			nextRoles.add("counsellor");

			AutomationUsers automationUser = automationUserService.getByUserId(accessToken.getPreferredUsername());
			if (automationUser != null) {
				isAutomated = true;
			}
		} else if (roles.contains("telecaller")) {
			role = "telecaller";
			nextRoles.add("manager");
			nextRoles.add("counsellor");
		} else if (roles.contains("counseller")) {
			nextRoles.add("telecaller");
			role = "counsellor";
			nextRoles.add("manager");
		}
		model.addObject("isAutomated", isAutomated);
		HttpHeaders header = new HttpHeaders();
		MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
		body.add("grant_type", "password");
		body.add("client_secret", keycloackClientSecret);
		body.add("username", adminUserName);
		body.add("password", adminPassword);
		body.add("client_id", "admin-cli");

		HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(body, header);

		@SuppressWarnings("rawtypes")
		HashMap response = restTemplate.postForObject(keycloackUrl + "/realms/master/protocol/openid-connect/token",
				entity, HashMap.class);
		String adminAccessToken = response.get("access_token").toString();

		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.set("Authorization", "Bearer " + adminAccessToken);

		List<Object> nextUsers = new ArrayList<>();

		for (String nextRole : nextRoles) {
			ResponseEntity<Object> userResponse = restTemplate.exchange(
					keycloackUrl + "/admin/realms/crmbot/clients/" + crmbotClientId + "/roles/" + nextRole + "/users",
					HttpMethod.GET, new HttpEntity<>(httpHeaders), Object.class);
			try {
				List<LinkedHashMap<String, Object>> userList = (List<LinkedHashMap<String, Object>>) userResponse
						.getBody();
				for (LinkedHashMap<String, Object> userMap : userList) {
					try {
						UserMaster user = this.userMasterService.getByUserName(userMap.get("username").toString());
						if (user != null) {
							userMap.put("isActiveOnCRM", user.getIsActive());
						}
					} catch (Exception e) {
						e.printStackTrace();
						userMap.put("isActiveOnCRM", false);
					}

				}
				nextUsers.add(userResponse.getBody());
			} catch (Exception e) {
				e.printStackTrace();
			}

		}

		List<FacebookLeadConfigs> facebookLeadConfigs = this.facebookLeadConfigService.getAllByIsActive(true);
		model.addObject("campaigns", facebookLeadConfigs);
		UserMaster userDetails = this.userMasterService.getByUserName(accessToken.getPreferredUsername());
		if (userDetails != null)
			model.addObject("isUserActive", userDetails.getIsActive());
		else
			model.addObject("isUserActive", true);

		List<Course> courses = this.courseService.getAll();

		model.addObject("courses", courses);

		List<Platforms> sourceList = this.platfromService.getAll();
		model.addObject("platforms", sourceList);

		model.addObject("prefferedUserName", accessToken.getPreferredUsername());
		model.addObject("role", role);
		model.addObject("userName", accessToken.getName());
		model.addObject("nextRoles", nextRoles);
		model.addObject("nextUsers", nextUsers);
		model.addObject("isAdmin", isAdmin);
		model.addObject("userEmail", accessToken.getPreferredUsername());
		model.addObject("userName", accessToken.getName());
		model.addObject("userId", accessToken.getSubject());

		role = roles.stream().findFirst().orElse(null);

		List<RoleModuleAccess> access = this.roleModuleAccessService.getByRole(role);
		model.addObject("access", access);

		return model;
	}

	@RequestMapping("report")
	@ResponseBody
	public Callable<ResponseEntity<Resource>> report(@RequestParam(value = "page") int page,
			@RequestParam(value = "size") int size, @RequestParam(value = "sorting") String sorting,
			@RequestParam(value = "desc") boolean desc, @RequestParam(value = "taskType") String taskType,
			@RequestParam(value = "leadPlatform") String leadPlatform,
			@RequestParam(value = "assignee") String assignee, @RequestParam(value = "fromDate") String fromDate,
			@RequestParam(value = "toDate") String toDate, Principal principal) {

		KeycloakAuthenticationToken token = (KeycloakAuthenticationToken) principal;
		AccessToken accessToken = token.getAccount().getKeycloakSecurityContext().getToken();

		Pageable requestedPage = PageRequest.of(page, 100000, desc ? Sort.by(sorting).descending() : Sort.by(sorting));
		FilterRequests filterRequests = new FilterRequests();
		filterRequests.setLeadPlatform(leadPlatform);
		filterRequests.setAssignee(assignee);
		filterRequests.setFromDate(fromDate);
		filterRequests.setToDate(toDate);
		filterRequests.setIsAdmin(true);
		filterRequests.setIsManager(false);
		filterRequests.setIsTeleCaller(false);
		filterRequests.setIsCounsellor(false);
		filterRequests.setIsAllTask(true);
		// filterRequests.setRole("admin");
		filterRequests.setIsMyTask(false);
		filterRequests.setUserName(accessToken.getPreferredUsername());
		List<ActiveTask> tasks = new ArrayList<>();
		if (taskType.equalsIgnoreCase(Constants.meetingTask)) {
			tasks = this.activeTaskService.getTodaysUnderCounsellingTaskAdmin(accessToken.getPreferredUsername());

			tasks.addAll(
					this.activeTaskService.getNotTodaysUnderCounsellingTaskAdmin(accessToken.getPreferredUsername()));

		} else {
			tasks = this.activeTaskService.getAllTasks(FilterSpecification.filter(filterRequests));
		}

		try {
			String fileName = "test.xlsx";
			ByteArrayInputStream in = excelHelper.taskToExcel(tasks);
			return () -> {
				return ResponseEntity.ok().contentType(MediaType.parseMediaType("application/octet-stream"))
						.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
						.body(new InputStreamResource(in));
			};
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}

	@RequestMapping("automation")
	public ModelAndView automationBySource(Principal principal) {
		KeycloakAuthenticationToken token = (KeycloakAuthenticationToken) principal;
		AccessToken accessToken = token.getAccount().getKeycloakSecurityContext().getToken();
		Boolean isAdmin = false;
		ModelAndView model = new ModelAndView();

		Set<String> roles = token.getAccount().getRoles();

		String role = roles.stream().findFirst().orElse(null);

		Boolean isAutomated = false;
		List<String> nextRoles = new ArrayList<>(Arrays.asList("admin", "manager", "telecaller", "counsellor"));

		if (role.equalsIgnoreCase("admin") || roles.contains(Constants.supervisor)) {

			List<Automation> autoamtions = this.automationService.getByIsActive(true);
			if (autoamtions.size() > 0) {
				isAutomated = true;
			}
		} else if (role.equalsIgnoreCase("manager")) {

			AutomationUsers automationUser = automationUserService.getByUserId(accessToken.getPreferredUsername());
			if (automationUser != null) {
				isAutomated = true;
			}

		}
		model.addObject("isAutomated", isAutomated);
		HttpHeaders header = new HttpHeaders();
		MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
		body.add("grant_type", "password");
		body.add("client_secret", keycloackClientSecret);
		body.add("username", adminUserName);
		body.add("password", adminPassword);
		body.add("client_id", "admin-cli");

		HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(body, header);

		@SuppressWarnings("rawtypes")
		HashMap response = restTemplate.postForObject(keycloackUrl + "/realms/master/protocol/openid-connect/token",
				entity, HashMap.class);
		String adminAccessToken = response.get("access_token").toString();

		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.set("Authorization", "Bearer " + adminAccessToken);

		List<Object> nextUsers = new ArrayList<>();

		ResponseEntity<List> userResponse = restTemplate.exchange(keycloackUrl + "/admin/realms/crmbot/groups",
				HttpMethod.GET, new HttpEntity<>(httpHeaders), List.class);

		try {
			List<Groups> groups = userResponse.getBody();

			model.addObject("groups", groups);

		} catch (Exception e) {
			e.printStackTrace();
		}

		model.addObject("nextRoles", nextRoles);
		model.addObject("nextUsers", nextUsers);

		model.addObject("automation", true);

		List<Platforms> sourceList = this.platfromService.getAll();
		model.addObject("sourceList", sourceList);
		List<Course> courseList = this.courseService.getAll();
		model.addObject("courseList", courseList);

		List<FacebookLeadConfigs> capmaigns = this.facebookLeadConfigService.getAll();
		model.addObject("capmaignsList", capmaigns);

		model.addObject("role", role);
		model.addObject("isAdmin", isAdmin);
		model.setViewName("automation-main");

		UserMaster userDetails = this.userMasterService.getByUserName(accessToken.getPreferredUsername());
		if (userDetails != null)
			model.addObject("isUserActive", userDetails.getIsActive());
		else
			model.addObject("isUserActive", true);
		model.addObject("prefferedUserName", accessToken.getPreferredUsername());
		List<RoleModuleAccess> access = this.roleModuleAccessService.getByRole(role);
		model.addObject("access", access);
		return model;
	}

	@GetMapping("mytask-count")
	@ResponseBody
	public ResponseEntity<Long> myTaskCount(Principal principal) {
		KeycloakAuthenticationToken token = (KeycloakAuthenticationToken) principal;
		AccessToken accessToken = token.getAccount().getKeycloakSecurityContext().getToken();
		Long myTask = activeTaskService.countOfMyTask(accessToken.getPreferredUsername());
		return new ResponseEntity<Long>(myTask, HttpStatus.OK);
	}

	@GetMapping("/leads-summary")
	public ModelAndView darshana(Principal principal) {
		KeycloakAuthenticationToken token = (KeycloakAuthenticationToken) principal;
		AccessToken accessToken = token.getAccount().getKeycloakSecurityContext().getToken();

		Set<String> roles = token.getAccount().getRoles();

		String role = roles.stream().findFirst().orElse(null);

		String name = accessToken.getPreferredUsername();
		ModelAndView model = new ModelAndView();
		Map<Integer, Long> yearCount = new HashMap<>();

		String roleToPass = "";
		if (roles.contains("manager")) {
			roleToPass = "manager_name";
		} else if (roles.contains("telecaller")) {
			roleToPass = "telecaller_name";

		} else if (roles.contains("counsellor")) {
			roleToPass = "counsellor_name";

		}

		List<RoleModuleAccess> access = this.roleModuleAccessService.getByRole(role);
		model.addObject("access", access);

		boolean isAdminLevelData = this.roleModuleAccessService.checkIfHasAdminAccess("LEAD SUMMARY", role);

		if (roles.contains("admin") || roles.contains("supervisor") || isAdminLevelData)
			yearCount = this.activeTaskService.getYearlyTaskCounts(null, null);
		else {
			yearCount = this.activeTaskService.getYearlyTaskCounts(name, roleToPass);
		}
		List<Course> courses = this.courseService.getAll();
		if (isAdminLevelData) {
			roles = null;
		}
		Map<String, Long> counts = activeTaskService.countTaskStats(null, null, null, null, null, roles, name, false);

		model.addObject("tableSummary", counts);
		model.addObject("yearCount", yearCount);
		model.addObject("userName", name);
		model.addObject("courses", courses);
		model.addObject("role", role);
		model.setViewName("leads-summary");

		List<Message> messages = this.messageService.findAll();
		model.addObject("messages", messages);

		return model;
	}

	@RequestMapping({ "report-json" })
	@ResponseBody
	public ResponseEntity<List<ActiveTask>> reportJson(Principal principal) {
		KeycloakAuthenticationToken token = (KeycloakAuthenticationToken) principal;
		AccessToken accessToken = token.getAccount().getKeycloakSecurityContext().getToken();

		String role = "";
		Boolean isManager = false;
		Boolean isTelecaller = false;
		Boolean isCounsellor = false;
		Set<String> roles = token.getAccount().getRoles();
		if (roles.contains("admin") || roles.contains(Constants.supervisor)) {
			List<Automation> autoamtions = this.automationService.getByIsActive(true);
			role = "admin";
		} else if (roles.contains("manager")) {
			role = "manager";
			isManager = true;
		} else if (roles.contains("telecaller")) {
			role = "telecaller";
			isTelecaller = true;
		} else if (roles.contains("counsellor")) {
			role = "counsellor";
			isCounsellor = true;
		}

		FilterRequests filterRequests = new FilterRequests();
//	      filterRequests.setLeadPlatform(leadPlatform);
		// filterRequests.setAssignee(role);
//	      filterRequests.setFromDate(fromDate);
//	      filterRequests.setToDate(toDate);
		filterRequests.setIsAdmin(false);
		// filterRequests.setRole(role);
		filterRequests.setIsManager(isManager);
		filterRequests.setIsTeleCaller(isTelecaller);
		filterRequests.setIsCounsellor(isCounsellor);
		filterRequests.setIsAllTask(true);
		filterRequests.setIsMyTask(false);
		filterRequests.setUserName(accessToken.getPreferredUsername());
		// Pageable requestedPage = PageRequest.of(page, size,
		// Sort.by(sorting).descending());
		List tasks = this.activeTaskService.getAllTasks(FilterSpecification.filter(filterRequests));

		return new ResponseEntity<List<ActiveTask>>(tasks, HttpStatus.OK);
	}

	@RequestMapping("/leads-transfer")
	public ModelAndView leadsTransfer(Principal principal) {
		KeycloakAuthenticationToken token = (KeycloakAuthenticationToken) principal;
		AccessToken accessToken = token.getAccount().getKeycloakSecurityContext().getToken();

		Set<String> roles = token.getAccount().getRoles();

		String role = roles.stream().findFirst().orElse(null);

		ModelAndView model = new ModelAndView();

		model.addObject("role", role);
		model.addObject("userName", accessToken.getName());
		model.addObject("leadsTransfer", true);
		model.setViewName("mail-configuration");
		model.setViewName("leads-transfer");

		Boolean isAutomated = false;
		List<Automation> autoamtions = this.automationService.getByIsActive(true);
		if (autoamtions.size() > 0) {
			isAutomated = true;
		}
		model.addObject("isAutomated", isAutomated);

		List<String> nextRoles = new ArrayList<>();
		nextRoles.add("manager");
		nextRoles.add("telecaller");
		nextRoles.add("counsellor");

		HttpHeaders header = new HttpHeaders();
		MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
		body.add("grant_type", "password");
		body.add("client_secret", keycloackClientSecret);
		body.add("username", adminUserName);
		body.add("password", adminPassword);
		body.add("client_id", "admin-cli");

		HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(body, header);

		@SuppressWarnings("rawtypes")
		HashMap response = restTemplate.postForObject(keycloackUrl + "/realms/master/protocol/openid-connect/token",
				entity, HashMap.class);
		String adminAccessToken = response.get("access_token").toString();

		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.set("Authorization", "Bearer " + adminAccessToken);

		List<Course> courses = this.courseService.getAll();
		model.addObject("courses", courses);
		List<Platforms> platforms = platfromService.getAll();
		model.addObject("platforms", platforms);

		List<Object> nextUsers = new ArrayList<>();

		for (String nextRole : nextRoles) {
			ResponseEntity<Object> userResponse = restTemplate.exchange(
					keycloackUrl + "/admin/realms/crmbot/clients/" + crmbotClientId + "/roles/" + nextRole + "/users",
					HttpMethod.GET, new HttpEntity<>(httpHeaders), Object.class);
			try {
				List<LinkedHashMap<String, Object>> userList = (List<LinkedHashMap<String, Object>>) userResponse
						.getBody();
				for (LinkedHashMap<String, Object> userMap : userList) {
					try {
						UserMaster user = this.userMasterService.getByUserName(userMap.get("username").toString());
						if (user != null) {
							userMap.put("isActiveOnCRM", user.getIsActive());
						}
					} catch (Exception e) {
						e.printStackTrace();
						userMap.put("isActiveOnCRM", false);
					}

				}
				nextUsers.add(userResponse.getBody());
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
		model.addObject("nextUsers", nextUsers);
		model.addObject("nextRoles", nextRoles);

		List<RoleModuleAccess> access = this.roleModuleAccessService.getByRole(role);
		model.addObject("access", access);

		return model;
	}

	@GetMapping("getmessages")
	@ResponseBody
	public ResponseEntity<List<Message>> getMessages() {
		return new ResponseEntity<List<Message>>(this.messageService.findAll(), HttpStatus.OK);

	}

}
