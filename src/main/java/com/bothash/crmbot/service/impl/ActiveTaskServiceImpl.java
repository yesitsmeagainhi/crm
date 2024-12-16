package com.bothash.crmbot.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.bothash.crmbot.dto.FilterRequests;
import com.bothash.crmbot.dto.Groups;
import com.bothash.crmbot.dto.KeycloakUserResponse;
import com.bothash.crmbot.entity.ActiveTask;
import com.bothash.crmbot.entity.AutomationByCourse;
import com.bothash.crmbot.entity.AutomationBySource;
import com.bothash.crmbot.entity.LastAllocatedUser;
import com.bothash.crmbot.entity.UserMaster;
import com.bothash.crmbot.repository.ActiveTaskRepository;
import com.bothash.crmbot.service.ActiveTaskService;
import com.bothash.crmbot.service.AutomationByCourseService;
import com.bothash.crmbot.service.AutomationBySourceService;
import com.bothash.crmbot.service.LastAllocatedUserService;
import com.bothash.crmbot.service.UserMasterService;
import com.bothash.crmbot.spec.FilterSpecification;
import com.bothash.crmbot.spec.FilterSpecificationDashborad;

@Service
public class ActiveTaskServiceImpl implements ActiveTaskService{
	
	@Autowired
	private RestTemplate restTemplate;
	
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
	
	@Autowired
	private ActiveTaskRepository activeTaskRepository;
	
	@Autowired
	private LastAllocatedUserService lastAllocatedUserService;
	

	@Autowired
	private UserMasterService userMasterService;
	
	@Override
	public List<ActiveTask> saveAll(List<ActiveTask> tasks) {
		return activeTaskRepository.saveAll(tasks);
	}

	@Override
	public Page<ActiveTask> getAllTasks(Pageable pagerequest) {
		return activeTaskRepository.findByIsActive(true,pagerequest);
	}

	@Override
	public Page<ActiveTask> getMyTask(String role, String userName,Pageable pagerequest) {
		Page<ActiveTask> mytasks=null;
		if(role.equals("admin")) {
			mytasks=activeTaskRepository.findByAssigneeAndIsActive(role,true,pagerequest);
		}else {
			mytasks=activeTaskRepository.findByAssigneeAndOwnerAndIsActive(role,userName,true,pagerequest);
		}
		return mytasks;
	}

	@Override
	public ActiveTask getTaskById(Long taskId) {
		Optional<ActiveTask> optTask=activeTaskRepository.findById(taskId);
		if(optTask.isPresent()) {
			return optTask.get();
		}
		return null;
	}

	@Override
	public ActiveTask save(ActiveTask activeTask) {
		return activeTaskRepository.save(activeTask);
	}

	@Override
	public Page<ActiveTask> getMyTask(FilterRequests filterRequests, String role, String preferredUsername,
			Pageable requestedPage) {
		Page<ActiveTask> mytasks=null;
		if(role.equals("admin")) {
			mytasks=activeTaskRepository.findByAssigneeAndIsActive(FilterSpecification.filter(filterRequests),role,true,requestedPage);
		}else {
			mytasks=activeTaskRepository.findByAssigneeAndOwnerAndIsActive(FilterSpecification.filter(filterRequests),role,preferredUsername,true,requestedPage);
		}
		return mytasks;
	}

	@Override
	public Page<ActiveTask> getAllTasks(FilterRequests filterRequests, Pageable requestedPage,Boolean isFilter) {
		if(isFilter) {
			return activeTaskRepository.findAll(FilterSpecification.filter(filterRequests),requestedPage);
		}
		return activeTaskRepository.findAll(FilterSpecification.filter(filterRequests),requestedPage);
	}

	@Override
	public Page<ActiveTask> getManagerTask(String userName, Pageable requestedPage) {
		return activeTaskRepository.findByManagerName(userName,requestedPage);
	}

	@Override
	public Page<ActiveTask> getTeleCallerTask(String userName, Pageable requestedPage) {
		return activeTaskRepository.findByTelecallerName(userName,requestedPage);
	}

	@Override
	public Page<ActiveTask> getAllTasks(Specification<ActiveTask> filter, Pageable requestedPage) {
		return activeTaskRepository.findAll(filter,requestedPage);
	}

	@Override
	public List<ActiveTask> getAllTasks(Specification<ActiveTask> filter) {
		return activeTaskRepository.findAll(filter);
	}

	@Override
	public Long countOfTotalTask() {
		return activeTaskRepository.count();
	}

	@Override
	public Long countOfActiveTask() {
		return activeTaskRepository.countByIsActive(true);
	}

	@Override
	public Long countOfConvertedTask(Boolean isConverted) {
		return activeTaskRepository.countByIsConverted(isConverted);
	}

	@Override
	public Long countOfTodaysTask() {
		return activeTaskRepository.countByCreatedOnGreaterThanEqualAndCreatedOnLessThanEqual(LocalDateTime.now().withHour(0).withMinute(0).withSecond(1),LocalDateTime.now().withHour(23).withMinute(59).withSecond(59));
	}

	@Override
	public Long countOfTotalTaskByPlatform(String platform) {
		return activeTaskRepository.countByLeadPlatform(platform);
	}

	@Override
	public Long countOfTotalActiveTaskByPlatform(String platform) {
		return activeTaskRepository.countByLeadPlatformAndIsActive(platform,true);
	}

	@Override
	public Long countOfTotalConvertedTaskByPlatform(String platform, Boolean isConverted) {
		return activeTaskRepository.countByLeadPlatformAndIsConverted(platform,isConverted);
	}

	@Override
	public List<ActiveTask> getTaskByPhoneNumber(String phoneNumber) {
		return activeTaskRepository.findByPhoneNumberAndIsActiveAndCreatedOnGreaterThan(phoneNumber,true,LocalDateTime.of(2024, 1, 1, 0, 0));
	}

	@Override
	public Map<String,String> randomlyAssgin(ActiveTask task,String currentUserRole) {
		HttpHeaders header=new HttpHeaders();
		MultiValueMap<String,String> body= new  LinkedMultiValueMap<>();
		body.add("grant_type", "password");
		body.add("client_secret", keycloackClientSecret);
		body.add("username", adminUserName);
		body.add("password", adminPassword);
		body.add("client_id", "admin-cli");
		
		HttpEntity<MultiValueMap<String, String>> entity=new HttpEntity<>(body,header);
		
		@SuppressWarnings("rawtypes")
		HashMap response=restTemplate.postForObject(keycloackUrl+"/realms/master/protocol/openid-connect/token",entity, HashMap.class);
		String adminAccessToken=response.get("access_token").toString();
		
		HttpHeaders httpHeaders=new HttpHeaders();
		httpHeaders.set("Authorization", "Bearer "+adminAccessToken);
		List<Long> unclaimedCountArray=new ArrayList<>();
		List<LinkedHashMap> telecallers=new ArrayList<>();
		String manager="";
		
		ResponseEntity<List> groupResponse=restTemplate.exchange(keycloackUrl+"/admin/realms/crmbot/groups",HttpMethod.GET,new HttpEntity<>(httpHeaders),List.class);
		List<LinkedHashMap> groups=new ArrayList<>();
		try {
			groups=groupResponse.getBody();
					
		}catch(Exception e) {
			e.printStackTrace();
		}
		LastAllocatedUser lastAllocatedUser=this.lastAllocatedUserService.getFirst();
		Boolean lastAllocatedUserFound=false;
		Boolean userAllocated=false;
		Map<String,String> responseList =new HashMap<String,String>();
		int groupsIndex=0;
		for(LinkedHashMap group:groups) {
			if(group!=null && !userAllocated) {
				
				try {
					ResponseEntity<List> userResponse=restTemplate.exchange(keycloackUrl+"/admin/realms/crmbot/groups/"+group.get("id").toString()+"/members",HttpMethod.GET,new HttpEntity<>(httpHeaders),List.class);
					
					System.out.println(userResponse.getBody().toString());
					@SuppressWarnings("unchecked")
					List<LinkedHashMap> users=userResponse.getBody();
					for(int i=0;i<users.size();i++) {
						LinkedHashMap user=users.get(i);
						
						UserMaster existing=userMasterService.getByUserName(user.get("username").toString());
						
						ResponseEntity<List> rolesResponse=this.restTemplate.exchange(keycloackUrl+"/admin/realms/crmbot/users/"+user.get("id").toString()+"/role-mappings/clients/"+crmbotClientId,HttpMethod.GET,new HttpEntity<>(httpHeaders),List.class);
						
						List<LinkedHashMap> roles=rolesResponse.getBody();
						String userRole="";
						for(int j=0;j<roles.size();j++ ) {
							LinkedHashMap role =roles.get(j);
							if(role.get("name").equals("admin")) {
								userRole="admin";
								break;
							}else if(userRole.equals("") && (role.get("name").toString()).equals("manager")) {
								userRole="manager";
							}else if(userRole.equals("") && (role.get("name").toString()).equals("telecaller")) {
								userRole="telecaller";
							}else if(userRole.equals("") && (role.get("name").toString()).equals("counsellor")) {
								userRole="counsellor";
							}
						}
						
						if(userRole.equals("manager")) {
							try {
								manager=user.get("username").toString();
								responseList.put("managerName", manager);
							}catch(Exception e) {
								e.printStackTrace();
							}
							
							
						}else if(userRole.equals("telecaller")) {
							try {
								if(lastAllocatedUser!=null && !lastAllocatedUserFound) {
									if(lastAllocatedUser.getUserId().equals(user.get("email").toString())) {
										lastAllocatedUserFound=true;
										if(i==users.size()-1) {
											responseList.put("userEmail", users.get(0).get("username").toString());
											responseList.put("userName",users.get(0).get("firstName").toString()+users.get(0).get("lastName").toString());
											userAllocated=true;
											LastAllocatedUser lasUserAllocatedNew=new LastAllocatedUser();
											lasUserAllocatedNew.setUserId(users.get(0).get("email").toString());
											LastAllocatedUser savedUser=this.lastAllocatedUserService.save(lasUserAllocatedNew);
										}
									}
								}else if(lastAllocatedUserFound && !userAllocated && (existing==null || existing.getIsActive())){ // check if user kept as active for lead distribution or not
									
									responseList.put("userEmail", user.get("username").toString());
									responseList.put("userName",user.get("firstName").toString()+user.get("lastName").toString());
									userAllocated=true;
									LastAllocatedUser lasUserAllocatedNew=new LastAllocatedUser();
									lasUserAllocatedNew.setUserId(user.get("email").toString());
									LastAllocatedUser savedUser=this.lastAllocatedUserService.save(lasUserAllocatedNew);
								}if(i==users.size()-1 && groupsIndex == group.size()-1 && !userAllocated) {
									for(int userIndex=0;userIndex<users.size();userIndex++) {
										UserMaster existing2=userMasterService.getByUserName(users.get(userIndex).get("username").toString());
										if(existing2==null || existing2.getIsActive()) {
											responseList.put("userEmail", users.get(userIndex).get("username").toString());
											responseList.put("userName",users.get(userIndex).get("firstName").toString()+users.get(userIndex).get("lastName").toString());
											userAllocated=true;
											LastAllocatedUser lasUserAllocatedNew=new LastAllocatedUser();
											lasUserAllocatedNew.setUserId(users.get(userIndex).get("email").toString());
											LastAllocatedUser savedUser=this.lastAllocatedUserService.save(lasUserAllocatedNew);
											break;
										}
									}
									
								}
							}catch(Exception e) {
								e.printStackTrace();
							}
						}
						
						
					}
					
					
				}catch(Exception e) {
					e.printStackTrace();
				}
			}
		}
		
		return responseList;
	}
	
	
	public Map<String,String> assignUserRandomly(List<LinkedHashMap> users,int index){
		Map<String,String> responseList =new HashMap<String,String>();
		
		
		return responseList;
	}

	@Override
	public List<ActiveTask> getByOwner(String role,String userId) {
		return this.activeTaskRepository.findByAssigneeAndOwner(role, userId);
	}

	@Override
	public List<ActiveTask> getByRoleAndOwnerAndSource(String string, String userId, String parameter) {
		return activeTaskRepository.findByAssigneeAndOwnerAndIsActiveAndLeadPlatform(string, userId, true, parameter);
	}

	@Override
	public List<ActiveTask> getByRoleAndOwnerAndCourse(String string, String userId, String parameter) {
		return activeTaskRepository.findByAssigneeAndOwnerAndIsActiveAndCourse(string, userId, true, parameter);
	}

	@Override
	public Page<ActiveTask> getNotTodaysScheduledTaskByOwner(Pageable requestedPage,String owner) {
		LocalDateTime minTime=LocalDateTime.of(LocalDate.now(), LocalTime.of(0, 0, 0));
		LocalDateTime maxTime=LocalDateTime.of(LocalDate.now(), LocalTime.of(23, 59, 59));
		return activeTaskRepository.findAllByIsScheduledAndOwnerAndScheduleTimeGreaterThanOrScheduleTimeLessThan(true,owner,maxTime,minTime,requestedPage);
	}

	@Override
	public Page<ActiveTask> getUnderCounsellingTaskByManager(Pageable requestedPage, String preferredUsername) {
		return activeTaskRepository.findByManagerNameAndAssigneeAndIsActive(preferredUsername,"counsellor",true, requestedPage);
	}

	@Override
	public Page<ActiveTask> getNotTodaysScheduledTaskByManager(Pageable requestedPage, String preferredUsername) {
		LocalDateTime minTime=LocalDateTime.of(LocalDate.now(), LocalTime.of(0, 0, 0));
		LocalDateTime maxTime=LocalDateTime.of(LocalDate.now(), LocalTime.of(23, 59, 59));
		return activeTaskRepository.findByManagerNameAndIsScheduledAndScheduleTimeGreaterThanOrScheduleTimeLessThan(preferredUsername, true,maxTime,minTime,requestedPage);
	}

	

	@Override
	public Page<ActiveTask> getNotTodaysScheduledTaskForAdmin(Pageable requestedPage) {
		LocalDateTime minTime=LocalDateTime.of(LocalDate.now(), LocalTime.of(0, 0, 0));
		LocalDateTime maxTime=LocalDateTime.of(LocalDate.now(), LocalTime.of(23, 59, 59));
		return activeTaskRepository.findByIsScheduledAndIsActiveAndScheduleTimeGreaterThanOrScheduleTimeLessThan(true,true,maxTime,minTime,requestedPage);
	}

	@Override
	public Page<ActiveTask> getTodaysScheduledTaskByOwner(Pageable requestedPage, String preferredUsername) {
		LocalDateTime yesterday=LocalDateTime.of(LocalDate.now().minusDays(1), LocalTime.of(23, 59, 59));
		LocalDateTime tomorrow=LocalDateTime.of(LocalDate.now().plusDays(1), LocalTime.of(0, 0,1));
		return activeTaskRepository.findAllByIsScheduledAndOwnerAndScheduleTimeLessThanAndScheduleTimeGreaterThan(true,preferredUsername,tomorrow,yesterday,requestedPage);
	}

	@Override
	public Page<ActiveTask> getTodaysScheduledTaskByManager(Pageable requestedPage, String preferredUsername) {
		LocalDateTime yesterday=LocalDateTime.of(LocalDate.now().minusDays(1), LocalTime.of(23, 59, 59));
		LocalDateTime tomorrow=LocalDateTime.of(LocalDate.now().plusDays(1), LocalTime.of(0, 0,1));
		return activeTaskRepository.findByManagerNameAndIsScheduledAndScheduleTimeLessThanAndScheduleTimeGreaterThan(preferredUsername,true,tomorrow,yesterday,requestedPage);
	}

	@Override
	public Page<ActiveTask> getTodaysScheduledTaskForAdmin(Pageable requestedPage) {
		LocalDateTime yesterday=LocalDateTime.of(LocalDate.now().minusDays(1), LocalTime.of(23, 59, 59));
		LocalDateTime tomorrow=LocalDateTime.of(LocalDate.now().plusDays(1), LocalTime.of(0, 0,1));
		return activeTaskRepository.findByIsScheduledAndIsActiveAndScheduleTimeLessThanAndScheduleTimeGreaterThan(true,true,tomorrow,yesterday,requestedPage);
	}

	@Override
	public Page<ActiveTask> getTodaysUnderCounsellingTaskByManager(Pageable requestedPage, String preferredUsername) {
		LocalDateTime yesterday=LocalDateTime.of(LocalDate.now().minusDays(1), LocalTime.of(23, 59, 59));
		LocalDateTime tomorrow=LocalDateTime.of(LocalDate.now().plusDays(1), LocalTime.of(0, 0,1));
		return activeTaskRepository.findByManagerNameAndAssigneeAndIsActiveAndIsScheduledAndScheduleTimeLessThanAndScheduleTimeGreaterThan(preferredUsername,"counsellor",true,true,tomorrow,yesterday, requestedPage);
	}

	@Override
	public Page<ActiveTask> getNotTodaysUnderCounsellingTaskByManager(Pageable requestedPage2,
			String preferredUsername) {
		LocalDateTime minTime=LocalDateTime.of(LocalDate.now(), LocalTime.of(0, 0, 0));
		LocalDateTime maxTime=LocalDateTime.of(LocalDate.now(), LocalTime.of(23, 59, 59));
		return activeTaskRepository.findByManagerNameAndAssigneeAndIsActiveAndScheduleTimeGreaterThanOrScheduleTimeLessThan(preferredUsername,"counsellor",true,maxTime,minTime, requestedPage2);
	}

	@Override
	public Page<ActiveTask> getTodaysUnderCounsellingTaskAdmin(Pageable requestedPage, String preferredUsername) {
		LocalDateTime yesterday=LocalDateTime.of(LocalDate.now().minusDays(1), LocalTime.of(23, 59, 59));
		LocalDateTime tomorrow=LocalDateTime.of(LocalDate.now().plusDays(1), LocalTime.of(0, 0,1));
		return activeTaskRepository.findByAssigneeAndIsActiveAndIsScheduledAndScheduleTimeLessThanAndScheduleTimeGreaterThan("counsellor",true,true,tomorrow,yesterday, requestedPage);
	}

	@Override
	public Page<ActiveTask> getNotTodaysUnderCounsellingTaskAdmin(Pageable requestedPage2, String preferredUsername) {
		LocalDateTime minTime=LocalDateTime.of(LocalDate.now(), LocalTime.of(0, 0, 0));
		LocalDateTime maxTime=LocalDateTime.of(LocalDate.now(), LocalTime.of(23, 59, 59));
		return activeTaskRepository.findByAssigneeAndIsActiveAndScheduleTimeGreaterThanOrScheduleTimeLessThan("counsellor",true,maxTime,minTime, requestedPage2);
	}

	@Override
	public Long countOfMyTask(String userName) {
		return activeTaskRepository.countByIsActiveAndOwner(true,userName);
	}

	@Override
	public List<ActiveTask> getGraphs(FilterRequests filterRequests) {
		return activeTaskRepository.findAll(FilterSpecificationDashborad.filter(filterRequests));
	}

	@Override
	public Long countOfPendingTaskByCaller(String telecallerName) {
		return activeTaskRepository.countByOwnerAndIsClaimedAndIsActiveAndCreatedOnGreaterThan(telecallerName,false,true,LocalDateTime.of(2024, 3, 1, 0, 0));
	}

	@Override
	public Long countOfProccessedTaskByCaller(String telecallerName) {
		return activeTaskRepository.countByOwnerAndIsClaimedAndIsActiveAndCreatedOnGreaterThan(telecallerName,true,true,LocalDateTime.of(2024, 3, 1, 0, 0));
	}

	@Override
	public Long countOfScheduledTaskByCaller(String telecallerName) {
		return activeTaskRepository.countByOwnerAndIsScheduledAndIsActive(telecallerName,true,true);
	}

	@Override
	public Long countOfCompletedTaskByCaller(String telecallerName) {
		return activeTaskRepository.countByTelecallerNameAndIsConvertedAndCreatedOnGreaterThan(telecallerName,true,LocalDateTime.of(2024, 3, 1, 0, 0));
	}

	@Override
	public Long countOfCounselledTaskByCaller(String telecallerName) {
		return activeTaskRepository.countByTelecallerNameAndIsCounsellingDoneAndCreatedOnGreaterThan(telecallerName,true,LocalDateTime.of(2024, 3, 1, 0, 0));
	}

}
