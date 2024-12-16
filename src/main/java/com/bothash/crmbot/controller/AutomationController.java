package com.bothash.crmbot.controller;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import org.json.JSONArray;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.keycloak.representations.AccessToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.ModelAndView;

import com.bothash.crmbot.dto.AutomationGroupReuqest;
import com.bothash.crmbot.dto.Groups;
import com.bothash.crmbot.entity.ActiveTask;
import com.bothash.crmbot.entity.Automation;
import com.bothash.crmbot.entity.AutomationByCampaign;
import com.bothash.crmbot.entity.AutomationByCourse;
import com.bothash.crmbot.entity.AutomationBySource;
import com.bothash.crmbot.entity.AutomationUsers;
import com.bothash.crmbot.entity.Course;
import com.bothash.crmbot.entity.FacebookLeadConfigs;
import com.bothash.crmbot.entity.HistoryEvents;
import com.bothash.crmbot.entity.Platforms;
import com.bothash.crmbot.repository.ActiveTaskRepository;
import com.bothash.crmbot.service.ActiveTaskService;
import com.bothash.crmbot.service.AutomationByCampaignService;
import com.bothash.crmbot.service.AutomationByCourseService;
import com.bothash.crmbot.service.AutomationBySourceService;
import com.bothash.crmbot.service.AutomationService;
import com.bothash.crmbot.service.AutomationUserService;
import com.bothash.crmbot.service.CourseService;
import com.bothash.crmbot.service.FacebookLeadConfigService;
import com.bothash.crmbot.service.HistoryEventsService;
import com.bothash.crmbot.service.PlatformService;

@Controller
@RequestMapping("/automation/")
public class AutomationController {
	
	@Autowired
	private AutomationByCourseService automationByCourseService;
	
	@Autowired
	private AutomationBySourceService automationBySourceService;
	
	@Autowired
	private AutomationByCampaignService automationByCampaignService;
	
	@Autowired
	private PlatformService platformService;
	
	@Autowired
	private CourseService courseService;
	
	@Autowired
	private FacebookLeadConfigService facebookLeadConfigService;
	
	@Autowired
	private RestTemplate restTemplate;
	@Autowired
	private AutomationUserService automationUserService;
	
	@Autowired
	private HistoryEventsService historyEventsService;
	
	@Autowired
	private AutomationService automationService;
	
	@Autowired
	private ActiveTaskService activeTaskService;
	
	@Autowired
	private ActiveTaskRepository activeTaskRepository;
	
	
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
	
	@GetMapping("course-assigned-user")
	public ModelAndView assignedUserByCourse(@RequestParam Long courseId){
		List<AutomationByCourse> allocatedUsers= automationByCourseService.getByCourseId(courseId);
		ModelAndView model= new ModelAndView();
		model.addObject("allocatedUsers", allocatedUsers);
		model.setViewName("automation-assigned-users");
		return model;
	}
	
	@GetMapping("source-assigned-user")
	public ModelAndView assignedUserBySource(@RequestParam Long sourceId){
		List<AutomationBySource> allocatedUsers= automationBySourceService.getBySourceId(sourceId);
		ModelAndView model= new ModelAndView();
		model.addObject("allocatedUsers", allocatedUsers);
		model.setViewName("automation-assigned-users");
		return model;
	}
	
	@GetMapping("campaign-assigned-user")
	public ModelAndView assignedUserByCampaign(@RequestParam Long campaignId){
		List<AutomationByCampaign> allocatedUsers= automationByCampaignService.getByCampaignId(campaignId);
		ModelAndView model= new ModelAndView();
		model.addObject("allocatedUsers", allocatedUsers);
		model.setViewName("automation-assigned-users");
		return model;
	}
	
	@PostMapping("save")
	public ModelAndView save(@RequestParam String paramter,@RequestBody AutomationGroupReuqest automationGroupReuqest){
		ModelAndView model= new ModelAndView();	
		
		
		if(paramter.equals("Source")) {
			
			AutomationBySource automationBySource=this.automationBySourceService.getBySourceIdAndGroupId(automationGroupReuqest.getParameterId(),automationGroupReuqest.getGroupId());
							
			if(automationBySource==null) {
				automationBySource=new AutomationBySource();
			}else {
				return null;
			}
			
			automationBySource.setIsActive(true);
			automationBySource.setIsLastAllocated(false);
			automationBySource.setGroupId(automationGroupReuqest.getGroupId());
			automationBySource.setGroupName(automationGroupReuqest.getGroupName());
			Platforms platform=this.platformService.getById(automationGroupReuqest.getParameterId());
			
			automationBySource.setPlatforms(platform);
			
			AutomationBySource savedAutomationBySource = this.automationBySourceService.save(automationBySource);
			
			List<AutomationBySource> allocatedUsers= automationBySourceService.getBySourceId(platform.getId());
			model.addObject("allocatedUsers", allocatedUsers);
			
		}else if(paramter.equals("Course")) {
			AutomationByCourse automationByCourse=this.automationByCourseService.getByCourseIdAndGroupId(automationGroupReuqest.getParameterId(),automationGroupReuqest.getGroupId());
			
			
			if(automationByCourse==null) {
				automationByCourse=new AutomationByCourse();
			}else {
				return null;
			}
			
			automationByCourse.setIsActive(true);
			automationByCourse.setIsLastAllocated(false);
			automationByCourse.setGroupId(automationGroupReuqest.getGroupId());
			automationByCourse.setGroupName(automationGroupReuqest.getGroupName());
			Course course = this.courseService.getById(automationGroupReuqest.getParameterId());
			
			automationByCourse.setCourse(course);
			
			AutomationByCourse savedAutomationByCourse = this.automationByCourseService.save(automationByCourse);
			List<AutomationByCourse> allocatedUsers= automationByCourseService.getByCourseId(course.getId());
			model.addObject("allocatedUsers", allocatedUsers);

		}else if(paramter.equals("Campaign")) {
			AutomationByCampaign automationByCampaign= this.automationByCampaignService.getByCampaignIdAndGroupId(automationGroupReuqest.getParameterId(),automationGroupReuqest.getGroupId());
			if(automationByCampaign==null) {
				automationByCampaign=new AutomationByCampaign();
			}else {
				return null;
			}
			
			automationByCampaign.setIsActive(true);
			automationByCampaign.setIsLastAllocated(false);
			automationByCampaign.setGroupId(automationGroupReuqest.getGroupId());
			automationByCampaign.setGroupName(automationGroupReuqest.getGroupName());
			FacebookLeadConfigs facebookLeadConfigs = this.facebookLeadConfigService.getById(automationGroupReuqest.getParameterId());
			
			automationByCampaign.setFacebookLeadConfigs(facebookLeadConfigs);
			
			AutomationByCampaign savedAutomationByCampaign = this.automationByCampaignService.save(automationByCampaign);
			List<AutomationByCampaign> allocatedUsers= automationByCampaignService.getByCampaignId(facebookLeadConfigs.getId());
			model.addObject("allocatedUsers", allocatedUsers);
		}
		
		
		model.setViewName("automation-assigned-users");
		
		return model;
			
	}
	
	@GetMapping("/automate")
	public ResponseEntity<String> automate(@RequestParam String parameter,Principal principal){
		KeycloakAuthenticationToken token = (KeycloakAuthenticationToken) principal;
		AccessToken accessToken = token.getAccount().getKeycloakSecurityContext().getToken();
		Set<String> roles=token.getAccount().getRoles();
		String role="";
		
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
		
		ResponseEntity<List> users=this.restTemplate.exchange(this.keycloackUrl+"/admin/realms/crmbot/users?username="+accessToken.getPreferredUsername(), HttpMethod.GET,new HttpEntity<>(httpHeaders),List.class);
		LinkedHashMap user=(LinkedHashMap) users.getBody().get(0);
		if(roles.contains("admin")) {
			role="admin";
			
			if(!parameter.equals("Remove")) {
				List<Automation> automate=this.automationService.getByIsActive(false);
				for(Automation a:automate) {
					a.setIsActive(true);
					this.automationService.save(a);
				}
			}else {
				List<Automation> automationList=this.automationService.getByIsActive(true);
				for(Automation a:automationList) {
					a.setIsActive(false);
					this.automationService.save(a);
				}
				return new ResponseEntity<String>("stopped",HttpStatus.OK);
			}
		}else if(roles.contains("manager")) {
			role="manager";
			AutomationUsers automationUser=automationUserService.getByUserId(accessToken.getPreferredUsername());
			if(!parameter.equals("Remove")) {
				if(automationUser==null) {
					automationUser=new AutomationUsers();
				}
				ResponseEntity<List> groups=this.restTemplate.exchange(keycloackUrl+"/admin/realms/crmbot/users/"+user.get("id").toString()+"/groups", HttpMethod.GET,new HttpEntity<>(httpHeaders),List.class);
				for(int i=0;i<groups.getBody().size();i++) {
					LinkedHashMap group=(LinkedHashMap) groups.getBody().get(i);
					automationUser.setGroupId(group.get("id").toString());
					automationUser.setGroupName(group.get("name").toString());
				}
				automationUser.setIsActive(true);
				automationUser.setUserId(accessToken.getPreferredUsername());
				automationUser.setUserName(accessToken.getName());
				automationUser.setParameter(parameter);
				automationUserService.save(automationUser);
			}else {
				if(automationUser!=null) {
					automationUser.setIsActive(false);
					automationUserService.save(automationUser);
				}
				return new ResponseEntity<String>("stopped",HttpStatus.OK);
			}
			
		}
		
		
		return new ResponseEntity<String>("started",HttpStatus.OK);
	}
	
	//@Scheduled(fixedRate = 120000)
	public void managerAutomation() {
		List<AutomationUsers> automationUsers=this.automationUserService.getAllActive();
		
		for(AutomationUsers a:automationUsers) {
			
			
			List<ActiveTask> tasks=new ArrayList<>();
			if(a.getParameter().equals("Source")) {
				AutomationBySource automationByGroup=this.automationBySourceService.getByGroupId(a.getGroupId());
				tasks=activeTaskService.getByRoleAndOwnerAndSource("manager",a.getUserId(),automationByGroup.getPlatforms().getName());
				
			}else if(a.getParameter().equals("Course")) {
				AutomationByCourse automationByGroup=this.automationByCourseService.getByGroupId(a.getGroupId());
				tasks=activeTaskService.getByRoleAndOwnerAndCourse("manager",a.getUserId(),automationByGroup.getCourse().getCourseName());
			}else {
				tasks=activeTaskService.getByOwner("manager", a.getUserId());
			}
			
			
			if(tasks.size()>0) {
				for(ActiveTask task:tasks) {
					List<Long> unclaimedCountArray=new ArrayList<>();
					List<LinkedHashMap> telecallers=new ArrayList<>();
					
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
					
					ResponseEntity<List> userResponse=restTemplate.exchange(keycloackUrl+"/admin/realms/crmbot/groups/"+a.getGroupId()+"/members",HttpMethod.GET,new HttpEntity<>(httpHeaders),List.class);
					try {
						List<LinkedHashMap> users=userResponse.getBody();
						for(int i=0;i<users.size();i++) {
							LinkedHashMap user=users.get(i);
							ResponseEntity<List> rolesResponse=this.restTemplate.exchange(keycloackUrl+"/admin/realms/crmbot/users/"+user.get("id").toString()+"/role-mappings/clients/"+crmbotClientId,HttpMethod.GET,new HttpEntity<>(httpHeaders),List.class);
							
							List<LinkedHashMap> roles=rolesResponse.getBody();
							for(int j=0;j<roles.size();j++ ) {
								LinkedHashMap role =roles.get(j);
								 if(role.get("name").toString().equals("telecaller")) {
									telecallers.add(user);
									Long countOfUnClaimedTask=this.activeTaskRepository.countByOwnerAndIsClaimedAndIsActiveAndCreatedOnGreaterThan(user.get("username").toString(), false, true,LocalDateTime.of(2024, 3,1,0,0));
									unclaimedCountArray.add(countOfUnClaimedTask);
									break;
								}
							}
						}
						
					}catch(Exception e) {
						e.printStackTrace();
					}
					try {
						Long minCount=Collections.min(unclaimedCountArray);
						
						int minIndex=unclaimedCountArray.indexOf(minCount);
						
						String telecallerUserName=telecallers.get(minIndex).get("username").toString();
						String telecallerName=telecallers.get(minIndex).get("firstName").toString()+telecallers.get(minIndex).get("lastName").toString();
						List<String> responseList =new ArrayList<>();
						task.setAssignee("telecaller");
						task.setAssignedTime(LocalDateTime.now());
						task.setOwner(telecallerUserName);
						task.setStatus("Assigned to "+telecallerName);
						task.setIsClaimed(false);
						task.setClaimTime(null);
						this.activeTaskRepository.save(task);
						
						HistoryEvents hisEvents=new HistoryEvents();
						hisEvents.setActiveTask(task);
						hisEvents.setUserName("Automation");
						hisEvents.setUserEmail("Automation");
						hisEvents.setUserId("Automation");
						hisEvents.setEvent("Task assigned to "+telecallerName);
						hisEvents.setRemark("Automatically assigned");
						historyEventsService.save(hisEvents);
						//responseList.add(manager);
						//responseList.add(telecallers.get(minIndex).get("firstName").toString()+telecallers.get(minIndex).get("lastName").toString());
					}catch(Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	@PutMapping("/deleteassignedgroups")
	public ModelAndView deleteAssignedGroups(@RequestBody AutomationGroupReuqest automationGroupReuqest){
		ModelAndView model =new ModelAndView();
		Long paramterId=1l;
		if(automationGroupReuqest.getGroupName().equalsIgnoreCase("Source")) {
			paramterId=automationBySourceService.delete(automationGroupReuqest.getParameterId());
			List<AutomationBySource> allocatedUsers= automationBySourceService.getBySourceId(paramterId);
			model.addObject("allocatedUsers", allocatedUsers);
		}else if(automationGroupReuqest.getGroupName().equalsIgnoreCase("Course")) {
			paramterId=automationByCourseService.delete(automationGroupReuqest.getParameterId());
			List<AutomationByCourse> allocatedUsers= automationByCourseService.getByCourseId(paramterId);
			model.addObject("allocatedUsers", allocatedUsers);
		}else if(automationGroupReuqest.getGroupName().equalsIgnoreCase("Campaign")) {
			paramterId=automationByCampaignService.delete(automationGroupReuqest.getParameterId());
			
			List<AutomationByCampaign> allocatedUsers= automationByCampaignService.getByCampaignId(paramterId);
			model.addObject("allocatedUsers", allocatedUsers);
		}
		model.setViewName("automation-assigned-users");
		return model;
	}

}
