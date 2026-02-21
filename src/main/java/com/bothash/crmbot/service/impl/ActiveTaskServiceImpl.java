package com.bothash.crmbot.service.impl;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import org.apache.commons.lang3.SerializationUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.bothash.crmbot.dto.DashboardBasicResponse;
import com.bothash.crmbot.dto.DashboardCardData;
import com.bothash.crmbot.dto.FilterRequests;
import com.bothash.crmbot.dto.Groups;
import com.bothash.crmbot.dto.KeycloakUserResponse;
import com.bothash.crmbot.dto.MonthLevel;
import com.bothash.crmbot.dto.TransferLeadsRequest;
import com.bothash.crmbot.dto.YearLevel;
import com.bothash.crmbot.entity.ActiveTask;
import com.bothash.crmbot.entity.AutomationByCourse;
import com.bothash.crmbot.entity.AutomationBySource;
import com.bothash.crmbot.entity.CounsellingDetails;
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

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ActiveTaskServiceImpl implements ActiveTaskService{
	
	@Autowired
	private RestTemplate restTemplate;
	
	@PersistenceContext
    private EntityManager entityManager;
	
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
	
	final List<String> usersToIgnore = Arrays.asList("dustbin@gmail.com","vikasji7676@gmail.com");
	
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
		// When no previous allocation exists, treat as if last user was found so first telecaller gets assigned
		Boolean lastAllocatedUserFound=(lastAllocatedUser==null);
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
										if(i==users.size()-1 && activeTaskRepository.countByIsActiveAndOwner(true, users.get(0).get("username").toString()) < 500) {
											responseList.put("userEmail", users.get(0).get("username").toString());
											responseList.put("userName",users.get(0).get("firstName").toString()+users.get(0).get("lastName").toString());
											userAllocated=true;
											LastAllocatedUser lasUserAllocatedNew=new LastAllocatedUser();
											lasUserAllocatedNew.setUserId(users.get(0).get("email").toString());
											LastAllocatedUser savedUser=this.lastAllocatedUserService.save(lasUserAllocatedNew);
										}
									}
								}else if(lastAllocatedUserFound && !userAllocated && (existing==null || existing.getIsActive()) && activeTaskRepository.countByIsActiveAndOwner(true, user.get("username").toString()) < 500){ // check if user kept as active for lead distribution and under 500 task cap
									
									responseList.put("userEmail", user.get("username").toString());
									responseList.put("userName",user.get("firstName").toString()+user.get("lastName").toString());
									userAllocated=true;
									LastAllocatedUser lasUserAllocatedNew=new LastAllocatedUser();
									lasUserAllocatedNew.setUserId(user.get("email").toString());
									LastAllocatedUser savedUser=this.lastAllocatedUserService.save(lasUserAllocatedNew);
								}if(i==users.size()-1 && groupsIndex == group.size()-1 && !userAllocated) {
									for(int userIndex=0;userIndex<users.size();userIndex++) {
										UserMaster existing2=userMasterService.getByUserName(users.get(userIndex).get("username").toString());
										if((existing2==null || existing2.getIsActive()) && activeTaskRepository.countByIsActiveAndOwner(true, users.get(userIndex).get("username").toString()) < 500) {
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
		return getCounsellorTasksTodayFirst(requestedPage.getPageNumber(),requestedPage.getPageSize(),preferredUsername);
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
		return getCounsellorTasksTodayFirst(requestedPage.getPageNumber(),requestedPage.getPageSize(),null);
	}

	
	public Page<ActiveTask> getCounsellorTasksTodayFirst(int page, int size, @Nullable String managerName) {
	    Pageable pageable = PageRequest.of(page, size);

	    Specification<ActiveTask> spec = (root, query, cb) -> {
	        List<Predicate> predicates = new ArrayList<>();

	        // Basic filters
	        predicates.add(cb.equal(cb.lower(root.get("assignee")), "counsellor"));
	        predicates.add(cb.isTrue(root.get("isActive")));
	        predicates.add(cb.isNotNull(root.get("scheduleTime"))); // Exclude NULL scheduleTime

	        if (managerName != null && !managerName.trim().isEmpty()) {
	            predicates.add(cb.like(cb.lower(root.get("managerName")), "%" + managerName.toLowerCase() + "%"));
	        }

	        // Time zone: IST (Asia/Kolkata)
	        ZoneId istZone = ZoneId.of("Asia/Kolkata");
	        ZonedDateTime istNow = ZonedDateTime.now(ZoneOffset.UTC).withZoneSameInstant(istZone);

	        LocalDateTime startOfTodayIST = istNow.toLocalDate().atStartOfDay();
	        LocalDateTime endOfTodayIST = istNow.toLocalDate().atTime(23, 59, 59);

	        // Priority expression:
	        // 1 for today
	        // 2 for future
	        // Exclude past tasks by adding to predicates
	        Predicate isToday = cb.between(root.get("scheduleTime"), startOfTodayIST, endOfTodayIST);
	        Predicate isFuture = cb.greaterThan(root.get("scheduleTime"), endOfTodayIST);
	        Predicate isTodayOrFuture = cb.or(isToday, isFuture);
	        predicates.add(isTodayOrFuture);

	        Expression<Object> sortPriority = cb.selectCase()
	            .when(isToday, 1)
	            .when(isFuture, 2)
	            .otherwise(3); // This should never hit because of `isTodayOrFuture`

	        query.orderBy(
	            cb.asc(sortPriority),             // Today first, then future
	            cb.asc(root.get("scheduleTime"))  // Among future, ascending order
	        );

	        return cb.and(predicates.toArray(new Predicate[0]));
	    };

	    return activeTaskRepository.findAll(spec, pageable);
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
		return activeTaskRepository.findAll(FilterSpecification.filter(filterRequests));
	}
	
	@Override
	public List<DashboardBasicResponse> countBySpecification(FilterRequests filterRequests,Boolean isScrutiny) {
		
		List<DashboardBasicResponse> basicResponseList = new ArrayList<>();
		String passedUserName = filterRequests.getUserName();
		
		if(filterRequests.getRole().equalsIgnoreCase("telecaller")) {
			filterRequests.setIsTeleCaller(true);
		}else if(filterRequests.getRole().equalsIgnoreCase("manager")) {
			filterRequests.setIsManager(true);
		}else if(filterRequests.getRole().equalsIgnoreCase("counsellor")) {
			filterRequests.setIsCounsellor(true);
		}
		
		List<Object> nextUsers=new ArrayList<>();
		List<String> nextRoles=new ArrayList<>();
		nextRoles.add(filterRequests.getRole());
		
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
		log.info(response+" TOLEN");
		String adminAccessToken=response.get("access_token").toString();
		
		HttpHeaders httpHeaders=new HttpHeaders();
		httpHeaders.set("Authorization", "Bearer "+adminAccessToken);
		for(String nextRole:nextRoles) {
			ResponseEntity<Object> userResponse=restTemplate.exchange(keycloackUrl+"/admin/realms/crmbot/clients/"+crmbotClientId+"/roles/"+nextRole+"/users",HttpMethod.GET,new HttpEntity<>(httpHeaders),Object.class);
			nextUsers.add(userResponse.getBody());
		}
		if(!isScrutiny) {
			if (passedUserName == null || passedUserName.isEmpty()) {
				for(int i=0;i<nextUsers.size();i++) {
					List<LinkedHashMap<String, Object>> hasList = (List)nextUsers.get(i);
					for(LinkedHashMap<String, Object> hashMap:hasList) {
						DashboardBasicResponse basicResponse =  new DashboardBasicResponse();
						String userName = hashMap.get("username").toString();
						String userNameForUI="";
						if(hashMap.containsKey("firstName")  && hashMap.get("firstName")!=null) {
							userNameForUI+=hashMap.get("firstName").toString();
						}
						if(hashMap.containsKey("lastName")  && hashMap.get("lastName")!=null) {
							userNameForUI+=" "+hashMap.get("lastName").toString();
						}
						
						basicResponse.setUserName(userNameForUI);
						basicResponse.setUserId(userName);
						filterRequests.setUserName(userName);
						basicResponse.setTodaysScheduled(this.getTodaysScheduledCount(filterRequests)); 
						basicResponse.setMissedSchedule(this.getMissedScheduledCount(filterRequests)); 
						basicResponse.setCounselled(getCounselledCount(filterRequests)); 
						basicResponse.setConverted(getAdmissionCount(filterRequests)); 
						basicResponseList.add(basicResponse);

					}
					
				}
			}else {
				DashboardBasicResponse basicResponse =  new DashboardBasicResponse();
				basicResponse.setUserName(filterRequests.getUserNameForUi());
				basicResponse.setUserId(passedUserName);
				filterRequests.setUserName(passedUserName);
				basicResponse.setTodaysScheduled(this.getTodaysScheduledCount(filterRequests)); 
				basicResponse.setMissedSchedule(this.getMissedScheduledCount(filterRequests)); 
				basicResponse.setCounselled(getCounselledCount(filterRequests)); 
				basicResponse.setConverted(getAdmissionCount(filterRequests)); 
				basicResponseList.add(basicResponse);
			}
		}else {
			log.info("adding scrutiny data");
			if (passedUserName == null || passedUserName.isEmpty()) {
				for(int i=0;i<nextUsers.size();i++) {
					List<LinkedHashMap<String, Object>> hasList = (List)nextUsers.get(i);
					for(LinkedHashMap<String, Object> hashMap:hasList) {
						DashboardBasicResponse basicResponse =  new DashboardBasicResponse();
						String userName = hashMap.get("username").toString();
						String userNameForUI="";
						if(hashMap.containsKey("firstName")  && hashMap.get("firstName")!=null) {
							userNameForUI+=hashMap.get("firstName").toString();
						}
						if(hashMap.containsKey("lastName")  && hashMap.get("lastName")!=null) {
							userNameForUI+=hashMap.get("lastName").toString();
						}
						
						basicResponse.setUserName(userNameForUI);
						basicResponse.setUserId(userName);
						filterRequests.setUserName(userName);
						basicResponse.setCold(getCountByLeadType(filterRequests, "COLD"));
						basicResponse.setHot(getCountByLeadType(filterRequests, "HOT"));
						basicResponse.setProspect(getCountByLeadType(filterRequests, "PROSPECT"));
						basicResponse.setBlank(getCountByLeadType(filterRequests, "BLANK"));
						basicResponse.setDustbin(getCountDustbin(filterRequests));
						basicResponse.setTotal(getCountTotal(filterRequests));
						basicResponse.setNoComment(getCountNoComment(filterRequests));
						basicResponse.setNoSchedule(getCountNoSchedule(filterRequests));
						basicResponse.setCounselled(getCounselledCount(filterRequests)); 
						basicResponse.setConverted(getAdmissionCount(filterRequests)); 
						basicResponseList.add(basicResponse);

					}
					
				}
			}else {
				DashboardBasicResponse basicResponse =  new DashboardBasicResponse();
				basicResponse.setUserId(passedUserName);
				basicResponse.setUserName(filterRequests.getUserNameForUi());
				filterRequests.setUserName(passedUserName);
				basicResponse.setCold(getCountByLeadType(filterRequests, "COLD"));
				basicResponse.setHot(getCountByLeadType(filterRequests, "HOT"));
				basicResponse.setProspect(getCountByLeadType(filterRequests, "PROSPECT"));
				basicResponse.setBlank(getCountByLeadType(filterRequests, "BLANK"));
				basicResponse.setDustbin(getCountDustbin(filterRequests));
				basicResponse.setTotal(getCountTotal(filterRequests));
				basicResponse.setNoComment(getCountNoComment(filterRequests));
				basicResponse.setNoSchedule(getCountNoSchedule(filterRequests));
				basicResponse.setCounselled(getCounselledCount(filterRequests)); 
				basicResponse.setConverted(getAdmissionCount(filterRequests)); 
				basicResponseList.add(basicResponse);
			}
		}
		
		
		
		basicResponseList.sort((a, b) -> {
		    // First, compare by converted (descending)
		    int convertedComparison = Integer.compare(b.getConverted(), a.getConverted());
		    if (convertedComparison != 0) {
		        return convertedComparison;
		    }

		    // If converted counts are equal, compare by counselled (descending)
		    return Integer.compare(b.getCounselled(), a.getCounselled());
		});

		return basicResponseList;
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

	@Override
	public int transferLeads(TransferLeadsRequest transferLeadsRequest) {
		
		return this.activeTaskRepository.transferLeads(transferLeadsRequest.getToUserName(), transferLeadsRequest.getToRole(), transferLeadsRequest.getFromUserName(), transferLeadsRequest.getFromRole(), transferLeadsRequest.getCourse(), transferLeadsRequest.getPlatform(), transferLeadsRequest.getLeadType(), transferLeadsRequest.getNumberOfLeads());
	}

	@Override
	public List<ActiveTask> getByOwnerAndActive(String role, String userName) {
		return this.activeTaskRepository.findByAssigneeAndOwnerAndIsActive(role, userName, true);
	}

	@Override
	public List<ActiveTask> getByOwnerAndActiveAndCourseAndPlatformAndLeadType(String role, String userName, String course,
			String platform,String leadType) {
		return this.activeTaskRepository.findByAssigneeAndOwnerAndIsActiveAndCourseAndLeadPlatformAndLeadType(role, userName, true,course,platform,leadType);
	}

	@Override
	public int getTodaysScheduledCount(FilterRequests filterRequests) {
		try {
			FilterRequests copyiedRequest = SerializationUtils.clone(filterRequests);
			copyiedRequest.setIsMyTask(false);
			copyiedRequest.setIsAdmin(false);
			copyiedRequest.setIsScheduled(true);
			copyiedRequest.setIsSeatConfirmed(null);
			copyiedRequest.setIsAllTask(true);
//			filterRequests.setScheduledTime(LocalDateTime.now().plusHours(5).plusMinutes(30));
			copyiedRequest.setScheduledTime(LocalDateTime.now().plusHours(5).plusMinutes(30));
			return this.activeTaskRepository.count(FilterSpecification.filter(copyiedRequest));
		}catch (Exception e) {
			log.error("error getting scheduled count");
			e.printStackTrace();
		}
		return 0;
		
	}
	
	@Override
	public int getMissedScheduledCount(FilterRequests filterRequestsPassed) {
		try {
			FilterRequests filterRequests = SerializationUtils.clone(filterRequestsPassed);
			filterRequests.setIsMyTask(true);
			filterRequests.setIsAdmin(false);
			filterRequests.setIsScheduledMissed(true);
			filterRequests.setIsSeatConfirmed(null);
			filterRequests.setScheduledTime(LocalDateTime.now().plusHours(5).plusMinutes(30));

			return this.activeTaskRepository.count(FilterSpecification.filter(filterRequests));
		}catch (Exception e) {
			log.error("error getting missed count");
			e.printStackTrace();
		}
		return 0;
		
	}
	
	@Override
	public int getCounselledCount(FilterRequests filterRequestsPassed) {
		try {
			FilterRequests filterRequests = SerializationUtils.clone(filterRequestsPassed);
			filterRequests.setIsAllTask(true);
			filterRequests.setIsAdmin(false);
			filterRequests.setIsSeatConfirmed(null);
			filterRequests.setIsCounselled(true);
			filterRequests.setIsDashboardFilter(true);
			if(filterRequests.getIsDateTypeChanged()!=null && filterRequests.getIsDateTypeChanged())
				filterRequests.setDateType("cousellingDate");
			return this.activeTaskRepository.count(FilterSpecification.filter(filterRequests));
		}catch (Exception e) {
			log.error("error getting missed count");
			e.printStackTrace();
		}
		return 0;
		
	}
	@Override
	public int getNotCounselledCount(FilterRequests filterRequestsPassed) {
		try {
			FilterRequests filterRequests = SerializationUtils.clone(filterRequestsPassed);
			filterRequests.setIsAllTask(true);
			filterRequests.setIsAdmin(false);
			filterRequests.setIsSeatConfirmed(null);
			filterRequests.setIsCounselled(false);
			filterRequests.setIsDashboardFilter(true);
			if(filterRequests.getIsDateTypeChanged()!=null && filterRequests.getIsDateTypeChanged())
				filterRequests.setDateType("cousellingDate");
			return this.activeTaskRepository.count(FilterSpecification.filter(filterRequests));
		}catch (Exception e) {
			log.error("error getting missed count");
			e.printStackTrace();
		}
		return 0;
		
	}
	
	@Override
	public int getAdmissionCount(FilterRequests filterRequestsPassed) {
		try {
			FilterRequests filterRequests = SerializationUtils.clone(filterRequestsPassed);
			filterRequests.setIsAllTask(true);
			filterRequests.setIsAdmin(false);
			filterRequests.setIsActive(false);
			filterRequests.setIsSeatConfirmed(null);
			filterRequests.setIsConverted(true);
			filterRequests.setIsDashboardFilter(true);
			if(filterRequests.getIsDateTypeChanged()!=null && filterRequests.getIsDateTypeChanged())
				filterRequests.setDateType("admissionDate");
			return this.activeTaskRepository.count(FilterSpecification.filter(filterRequests));
		}catch (Exception e) {
			log.error("error getting missed count");
			e.printStackTrace();
		}
		return 0;
		
	}
	
	@Override
	public int getCountByLeadType(FilterRequests filterRequestsPassed,String leadType) {
		try {
			FilterRequests filterRequests = SerializationUtils.clone(filterRequestsPassed);
			filterRequests.setIsAllTask(true);
			filterRequests.setIsAdmin(false);
			filterRequests.setIsSeatConfirmed(null);
			filterRequests.setLeadType(leadType);
			return this.activeTaskRepository.count(FilterSpecification.filter(filterRequests));
		}catch (Exception e) {
			log.error("error getting missed count");
			e.printStackTrace();
		}
		return 0;
		
	}
	
	@Override
	public int getCountTotal(FilterRequests filterRequestsPassed) {
		try {
			FilterRequests filterRequests = SerializationUtils.clone(filterRequestsPassed);
			filterRequests.setIsAllTask(true);
			filterRequests.setIsAdmin(false);
			filterRequests.setIsSeatConfirmed(null);
			filterRequests.setIsLeadSummary(true);
			return this.activeTaskRepository.count(FilterSpecification.filter(filterRequests));
		}catch (Exception e) {
			log.error("error getting missed count");
			e.printStackTrace();
		}
		return 0;
		
	}

	@Override
	public int getCountNoComment(FilterRequests filterRequestsPassed) {
		try {
			FilterRequests filterRequests = SerializationUtils.clone(filterRequestsPassed);
			filterRequests.setIsAllTask(true);
			filterRequests.setIsAdmin(false);
			filterRequests.setIsSeatConfirmed(null);
			filterRequests.setHasComments(false);
			return this.activeTaskRepository.count(FilterSpecification.filter(filterRequests));
		}catch (Exception e) {
			log.error("error getting missed count");
			e.printStackTrace();
		}
		return 0;
		
	}
	
	@Override
	public int getCountNoSchedule(FilterRequests filterRequestsPassed) {
		try {
			FilterRequests filterRequests = SerializationUtils.clone(filterRequestsPassed);
			filterRequests.setIsAllTask(true);
			filterRequests.setIsAdmin(false);
			filterRequests.setIsSeatConfirmed(null);
			filterRequests.setIsScheduled(false);
			filterRequests.setScheduledTime(LocalDateTime.now());
			return this.activeTaskRepository.count(FilterSpecification.filter(filterRequests));
		}catch (Exception e) {
			log.error("error getting missed count");
			e.printStackTrace();
		}
		return 0;
		
	}
	
	@Override
	public int getCountDustbin(FilterRequests filterRequestsPassed) {
		try {
			FilterRequests filterRequests = SerializationUtils.clone(filterRequestsPassed);
			filterRequests.setIsAllTask(true);
			filterRequests.setIsAdmin(false);
			filterRequests.setIsSeatConfirmed(null);
			filterRequests.setIsDustin(true);
			return this.activeTaskRepository.count(FilterSpecification.filter(filterRequests));
		}catch (Exception e) {
			log.error("error getting missed count");
			e.printStackTrace();
		}
		return 0;
		
	}

	@Override
	public DashboardCardData getDashBoardCardData(FilterRequests filterRequestsPassed) {
		filterRequestsPassed.setIsAllTask(true);
		filterRequestsPassed.setIsAdmin(false);
		filterRequestsPassed.setIsDashboardFilter(true);
		if(filterRequestsPassed.getRole()!=null) {
			if(filterRequestsPassed.getRole().equalsIgnoreCase("telecaller")) {
				filterRequestsPassed.setIsTeleCaller(true);
			}else if(filterRequestsPassed.getRole().equalsIgnoreCase("manager")) {
				filterRequestsPassed.setIsManager(true);
			}else if(filterRequestsPassed.getRole().equalsIgnoreCase("counsellor")) {
				filterRequestsPassed.setIsCounsellor(true);
			}
		}
		
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		SimpleDateFormat formatter2 = new SimpleDateFormat("yyyy-MM-dd");
		FilterRequests filterRequests1 = SerializationUtils.clone(filterRequestsPassed);
		FilterRequests filterRequests2 = SerializationUtils.clone(filterRequestsPassed);
		FilterRequests filterRequests3 = SerializationUtils.clone(filterRequestsPassed);
		FilterRequests filterRequests4 = SerializationUtils.clone(filterRequestsPassed);
		FilterRequests filterRequests5 = SerializationUtils.clone(filterRequestsPassed);
		
		DashboardCardData response = new DashboardCardData();
		
		LocalDate tofay= LocalDate.now();
		String formattedDate = tofay.format(DateTimeFormatter.ISO_LOCAL_DATE);
//		filterRequests1.setFromDate(formattedDate);
//		filterRequests1.setToDate(formattedDate);
//		filterRequests2.setFromDate(formattedDate);
//		filterRequests2.setToDate(formattedDate);
//		filterRequests3.setFromDate(formattedDate);
//		filterRequests3.setToDate(formattedDate);
		
		int todaysLead = this.activeTaskRepository.count(FilterSpecification.filter(filterRequests1));
		response.setTodaysLeads(todaysLead);
		
		filterRequests2.setIsCounselled(true);	
		filterRequests2.setToDate(null);
		
		if(filterRequests2.getIsDateTypeChanged()!=null && filterRequests2.getIsDateTypeChanged())
			filterRequests2.setDateType("cousellingDate");
		int todaysCounselled = this.activeTaskRepository.count(FilterSpecification.filter(filterRequests2));
		
		filterRequests3.setIsSeatConfirmed(true);
		int seatConfirmed = this.activeTaskRepository.count(FilterSpecification.filter(filterRequests3));
		
		filterRequests4.setIsConverted(true);
		filterRequests4.setIsActive(false);
		filterRequests4.setIsDashboardFilter(true);
		
		
		LocalDateTime endDateNew=null;
		if(filterRequests5.getToDate()!=null && filterRequests5.getToDate().length()>0) {
			Date endDate=null;
			
			try {
				endDate = formatter.parse(filterRequests5.getToDate().toString()+" 00:01");
				endDateNew=convertToLocalDateTimeViaInstant(endDate);
				endDateNew=endDateNew.minusHours(5).minusMinutes(30);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		LocalDateTime startDateNew=null;
		if(filterRequests5.getFromDate()!=null && filterRequests5.getFromDate().length()>0) {
			Date startDate=null;
			
			try { 
				startDate = formatter.parse(filterRequests5.getFromDate().toString()+" 00:01");
				startDateNew=convertToLocalDateTimeViaInstant(startDate);
				startDateNew=startDateNew.minusHours(5).minusMinutes(30);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		if(endDateNew!=null && startDateNew!=null && (endDateNew.equals(startDateNew) || endDateNew.toLocalDate().equals(LocalDate.now()))) {
			filterRequests5.setFromDate(null);
			filterRequests5.setToDate(null);
			filterRequests4.setFromDate(null);
			filterRequests4.setToDate(null);
		
		}
		if(filterRequests4.getIsDateTypeChanged()!=null && filterRequests4.getIsDateTypeChanged())
			filterRequests4.setDateType("admissionDate");
		
		filterRequests5.setIsActive(null);
		int totalLeads = this.activeTaskRepository.count(FilterSpecification.filter(filterRequests5));
		
		int converted = this.activeTaskRepository.count(FilterSpecification.filter(filterRequests4));
		
		response.setTodaysCounselled(todaysCounselled);
		response.setTodaysSeatConfirmed(seatConfirmed);
		response.setTotalAdmission(converted);
		response.setTotalLeads(totalLeads);
		
		return response;
	}
	public static LocalDateTime convertToLocalDateTimeViaInstant(Date dateToConvert) {
	    return dateToConvert.toInstant()
	      .atZone(ZoneId.systemDefault())
	      .toLocalDateTime();
	}

	
	@Override
	public Map<Integer, Long> getMonthlyTaskCounts(int year,String userName,String role) {
		List<Object[]> rows =  new ArrayList<>();
		if(userName!=null && role!=null) {
			rows = this.countTasksPerMonthByRole(year,role,userName);
		}else {
			rows = activeTaskRepository.countTasksPerMonth(year,usersToIgnore);
		}
	    
	    return rows.stream()
	        .collect(Collectors.toMap(
	            row -> ((Number) row[0]).intValue(),
	            row -> ((Number) row[1]).longValue()
	        ));
	}

	@Override	
	public Map<Integer, Long> getDailyTaskCounts(int year, int month,String userName,String role) {
		List<Object[]> rows =  new ArrayList<>();
		if(userName!=null && role!=null) {
			rows = this.countTasksPerDayByRole(year, month,role,userName);
			
		}
		else {
			rows = activeTaskRepository.countTasksPerDay(year, month,usersToIgnore);
		}
	    return rows.stream()
	        .collect(Collectors.toMap(
	            row -> ((Number) row[0]).intValue(),
	            row -> ((Number) row[1]).longValue()
	        ));
	}
	
	@Override
	public Map<Integer, Long> getYearlyTaskCounts(String userName,String role) {
		List<Object[]> results =  new ArrayList<>();
		if(userName!=null && role!=null) {
			results = this.countTasksPerYearByRole(role,userName);
		}else {
			results = activeTaskRepository.countTasksPerYear(usersToIgnore);
		}
        
        Map<Integer, Long> yearCounts = new HashMap<>();

        for (Object[] row : results) {
            Integer year = ((Number) row[0]).intValue();
            Long count = ((Number) row[1]).longValue();
            yearCounts.put(year, count);
        }

        return yearCounts;
    }
	
	@Override
	public Map<String, Long> countTaskStats(Integer year, Integer month, Integer day, String course, String leadType, Set<String> roles, String userName,Boolean isActive) {
	    CriteriaBuilder cb = entityManager.getCriteriaBuilder();

	    Long totalTasks = countWithPredicates(cb, year, month, day, course, leadType, null,roles,userName,isActive);

	    Long counselledTasks = countWithPredicates(cb, year, month, day, course, leadType, "COUNSELLED",roles,userName,isActive);

	    Long admissionDoneTasks = countWithPredicates(cb, year, month, day, course, leadType, "ADMISSION_DONE",roles,userName,isActive);

	    Long dPharmAdmissions = countWithPredicates(cb, year, month, day, "D.PHARM", leadType, "ADMISSION_DONE",roles,userName,isActive);

	    Long bPharmAdmissions = countWithPredicates(cb, year, month, day, "B.PHARM", leadType, "ADMISSION_DONE",roles,userName,isActive);
	    
	    Long gmnAdmissions = countWithPredicates(cb, year, month, day, "GNM", leadType, "ADMISSION_DONE",roles,userName,isActive);
	    
	    Long otherAdmissions = countWithPredicates(cb, year, month, day, "OTHERS", leadType, "ADMISSION_DONE",roles,userName,isActive);

	    Map<String, Long> result = new HashMap<>();
	    result.put("totalTasks", totalTasks);
	    result.put("counselledTasks", counselledTasks);
	    result.put("admissionDoneTasks", admissionDoneTasks);
	    result.put("dPharmAdmissions", dPharmAdmissions);
	    result.put("bPharmAdmissions", bPharmAdmissions);
	    result.put("otherAdmissions", otherAdmissions);
	    result.put("gmnAdmissions", gmnAdmissions);
	    return result;
	}


	
	private Long countWithPredicates(CriteriaBuilder cb,
	        Integer year,
	        Integer month,
	        Integer day,
	        String course,
	        String leadType,
	        String specialFilter, Set<String> roles, String userName,Boolean isActivePassed) {

	    CriteriaQuery<Long> query = cb.createQuery(Long.class);
	    Root<ActiveTask> root = query.from(ActiveTask.class);

	    List<Predicate> predicates = new ArrayList<>();
	    	
	    if(isActivePassed!=null && isActivePassed) {			
            predicates.add(cb.equal(root.get("isActive"),isActivePassed));
	    }
	    if(roles!=null && roles.contains("manager")) {
			final Predicate managerTask= cb.like(cb.lower(root.get("managerName")),"%"+userName+"%");
			predicates.add(managerTask);
		}else if(roles!=null && roles.contains("telecaller")) {
			final Predicate teleCallerTask= cb.like(cb.lower(root.get("telecallerName")),"%"+userName+"%");
			predicates.add(teleCallerTask);
		}else if(roles!=null && roles.contains("counsellor")) {
			final Predicate counsellorName= cb.like(cb.lower(root.get("counsellorName")),"%"+userName+"%");
			predicates.add(counsellorName);
		}
	    if (year != null) {
	        predicates.add(cb.equal(cb.function("YEAR", Integer.class, root.get("scheduleTime")), year));
	        predicates.add(cb.isNotNull(root.get("scheduleTime")));
	    }
	    if (month != null) {
	        predicates.add(cb.equal(cb.function("MONTH", Integer.class, root.get("scheduleTime")), month));
	    }
	    if (day != null) {
	        predicates.add(cb.equal(cb.function("DAY", Integer.class, root.get("scheduleTime")), day));
	    }

	    predicates.add(cb.or(cb.not(root.get("owner").in(usersToIgnore)),cb.isNull(root.get("owner"))));

	    
	    if (course != null && !course.isEmpty()) {
	    	if(!course.equalsIgnoreCase("OTHERS"))
	    		predicates.add(cb.equal(root.get("course"), course));
	    	else {
	    		 predicates.add(cb.and(
	    				 cb.notEqual(root.get("course"), "B.PHARM"),
	 	                cb.notEqual(root.get("course"), "D.PHARM"),
	 	               cb.notEqual(root.get("course"), "GNM")
	 	            ));
	    	}
	    }

	    if (leadType != null ) {
	        if (!leadType.equalsIgnoreCase("BLANK") && !leadType.isEmpty()) {
	            predicates.add(cb.equal(root.get("leadType"), leadType));
	        } else {
	            predicates.add(cb.or(
	                cb.isNull(root.get("leadType")),
	                cb.equal(root.get("leadType"), "")
	            ));
	        }
	    }

	    if (specialFilter != null) {
	        switch (specialFilter) {
	            case "COUNSELLED":
	            	Subquery<Long> commentSubquery = query.subquery(Long.class);
                    Root<CounsellingDetails> counsellingRoot = commentSubquery.from(CounsellingDetails.class);
                    	commentSubquery.select(cb.count(counsellingRoot))
                        .where(cb.equal(counsellingRoot.get("activeTask"), root));
                    predicates.add(cb.greaterThan(commentSubquery.getSelection(), cb.literal(0L)));
                    
	            	final Predicate isActive= cb.equal(root.get("isActive"),true);
					
	                predicates.add(isActive);
	                break;

	            case "ADMISSION_DONE":
	                predicates.add(cb.isTrue(root.get("isConverted")));
	                break;

	            default:
	                break;
	        }
	    }

	    query.select(cb.count(root)).where(predicates.toArray(new Predicate[0]));
	    return entityManager.createQuery(query).getSingleResult();
	}
	
	@Override
	public Page<ActiveTask> getFilteredTasks(
		    Integer year,
		    Integer month,
		    Integer day,
		    String course,
		    String leadType,
		    Predicate extraPredicate,
		    int page,
		    int size,Set<String> roles, String userName
		) {
		    CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		    CriteriaQuery<ActiveTask> query = cb.createQuery(ActiveTask.class);
		    Root<ActiveTask> root = query.from(ActiveTask.class);

		    List<Predicate> predicates = new ArrayList<>();

		    
		    if(roles!=null && roles.contains("manager")) {
				final Predicate managerTask= cb.like(cb.lower(root.get("managerName")),"%"+userName+"%");
				predicates.add(managerTask);
			}else if(roles!=null && roles.contains("telecaller")) {
				final Predicate teleCallerTask= cb.like(cb.lower(root.get("telecallerName")),"%"+userName+"%");
				predicates.add(teleCallerTask);
			}else if(roles!=null && roles.contains("counsellor")) {
				final Predicate counsellorName= cb.like(cb.lower(root.get("counsellorName")),"%"+userName+"%");
				predicates.add(counsellorName);
			}
		    if (year != null) {
		        predicates.add(cb.equal(cb.function("YEAR", Integer.class, root.get("scheduleTime")), year));
		        predicates.add(cb.isNotNull(root.get("scheduleTime")));
		    }
		    if (month != null) {
		        predicates.add(cb.equal(cb.function("MONTH", Integer.class, root.get("scheduleTime")), month));
		    }
		    if (day != null) {
		        predicates.add(cb.equal(cb.function("DAY", Integer.class, root.get("scheduleTime")), day));
		    }
		    if (course != null && !course.isEmpty()) {
		        predicates.add(cb.equal(root.get("course"), course));
		    }
		    if (leadType != null ) {
		        if (!leadType.equalsIgnoreCase("BLANK") && !leadType.isEmpty()) {
		            predicates.add(cb.equal(root.get("leadType"), leadType));
		        } else {
		            predicates.add(cb.or(
		                cb.equal(root.get("leadType"), ""),
		                cb.isNull(root.get("leadType"))
		            ));
		        }
		    }
		    if (extraPredicate != null) {
		        predicates.add(extraPredicate);
		    }
		    final Predicate isActive= cb.equal(root.get("isActive"),true);
		    predicates.add(isActive);
		    
		    predicates.add(cb.or(cb.not(root.get("owner").in(usersToIgnore)),cb.isNull(root.get("owner"))));
//		    predicates.add();
		    
		    query.where(predicates.toArray(new Predicate[0]));

		    // Sorting
		    query.orderBy(cb.desc(root.get("scheduleTime")));

		    // Fetch results
		    List<ActiveTask> content = entityManager.createQuery(query)
		            .setFirstResult(page * size)
		            .setMaxResults(size)
		            .getResultList();

		    // Count total
		    CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
		    Root<ActiveTask> countRoot = countQuery.from(ActiveTask.class);
		    countQuery.select(cb.count(countRoot))
		              .where(predicates.toArray(new Predicate[0]));

		    Long total = entityManager.createQuery(countQuery).getSingleResult();

		    return new PageImpl<>(content, PageRequest.of(page, size), total);
		}

	@Override
	public List<Object[]> countTasksPerYearByRole(String role, String userName) {
	    String sql = "SELECT YEAR(schedule_time) AS year, COUNT(*) AS count " +
	                 "FROM active_task " +
	                 "WHERE schedule_time IS NOT NULL AND " + role + " = :userName " +
	                 "AND ((owner NOT IN (:excludedOwners) or owner is NULL))" +
	                 " AND is_active=true "+
	                 "GROUP BY YEAR(schedule_time)";

	    Query query = entityManager.createNativeQuery(sql);
	    query.setParameter("userName", userName);
	    query.setParameter("excludedOwners", usersToIgnore);
	    return query.getResultList();
	}
	
	@Override
	public List<Object[]> countTasksPerMonthByRole(int year,String role, String userName) {
	    String sql = "SELECT MONTH(schedule_time) AS month, COUNT(*) AS count " +
	            "FROM active_task " +
	            "WHERE YEAR(schedule_time) = :year AND schedule_time IS NOT NULL AND " + role + " = :userName " +
	            "AND (owner NOT IN (:excludedOwners) or owner is NULL)" +
	            " AND is_active=true "+
	            "GROUP BY MONTH(schedule_time)";

	    Query query = entityManager.createNativeQuery(sql);
	    query.setParameter("userName", userName);
	    query.setParameter("year", year);
	    query.setParameter("excludedOwners", usersToIgnore);
	    return query.getResultList();
	}
	
	@Override
	public List<Object[]> countTasksPerDayByRole(int year,int month,String role, String userName) {
	    String sql = "SELECT DAY(schedule_time) AS day, COUNT(*) AS count FROM active_task WHERE YEAR(schedule_time) = :year "
	    		+ "AND MONTH(schedule_time) = :month AND "+ role + " = :userName " +
	    		"AND ((owner NOT IN (:excludedOwners) or owner is NULL))" 
	    		+" AND is_active=true "
	    		+  " GROUP BY DAY(schedule_time)";

	    Query query = entityManager.createNativeQuery(sql);
	    query.setParameter("userName", userName);
	    query.setParameter("year", year);
	    query.setParameter("month", month);
	    query.setParameter("excludedOwners", usersToIgnore);
	    return query.getResultList();
	}

	@Override
	public List<ActiveTask> getTodaysUnderCounsellingTaskAdmin(String preferredUsername) {
		LocalDateTime yesterday=LocalDateTime.of(LocalDate.now().minusDays(1), LocalTime.of(23, 59, 59));
		LocalDateTime tomorrow=LocalDateTime.of(LocalDate.now().plusDays(1), LocalTime.of(0, 0,1));
		return activeTaskRepository.findByAssigneeAndIsActiveAndIsScheduledAndScheduleTimeLessThanAndScheduleTimeGreaterThan("counsellor",true,true,tomorrow,yesterday, Sort.by(Sort.Direction.ASC, "scheduleTime"));
	
	}

	@Override
	public List<ActiveTask> getNotTodaysUnderCounsellingTaskAdmin(String preferredUsername) {
		LocalDateTime minTime=LocalDateTime.of(LocalDate.now(), LocalTime.of(0, 0, 0));
		LocalDateTime maxTime=LocalDateTime.of(LocalDate.now(), LocalTime.of(23, 59, 59));
		return activeTaskRepository.findByAssigneeAndIsActiveAndScheduleTimeGreaterThanOrScheduleTimeLessThan("counsellor",true,maxTime,minTime, Sort.by(Sort.Direction.ASC, "scheduleTime"));
	}

	@Override
	public long getTotalCount() {
		return this.activeTaskRepository.count();
	}


	

}
