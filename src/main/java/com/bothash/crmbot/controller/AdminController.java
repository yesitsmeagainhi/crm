package com.bothash.crmbot.controller;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import org.json.JSONArray;
import org.json.JSONObject;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.keycloak.representations.AccessToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.ModelAndView;

import com.bothash.crmbot.dto.Constants;
import com.bothash.crmbot.dto.DashboardBasicResponse;
import com.bothash.crmbot.dto.DashboardCardData;
import com.bothash.crmbot.dto.FilterRequests;
import com.bothash.crmbot.entity.ActiveTask;
import com.bothash.crmbot.entity.Automation;
import com.bothash.crmbot.entity.AutomationUsers;
import com.bothash.crmbot.entity.CommentMaster;
import com.bothash.crmbot.entity.Course;
import com.bothash.crmbot.entity.FacebookLeadConfigs;
import com.bothash.crmbot.entity.Modules;
import com.bothash.crmbot.entity.Platforms;
import com.bothash.crmbot.entity.RoleModuleAccess;
import com.bothash.crmbot.entity.TargetMails;
import com.bothash.crmbot.entity.UserMaster;
import com.bothash.crmbot.service.ActiveTaskService;
import com.bothash.crmbot.service.AutomationService;
import com.bothash.crmbot.service.AutomationUserService;
import com.bothash.crmbot.service.CloseTaskService;
import com.bothash.crmbot.service.CommentMasterService;
import com.bothash.crmbot.service.CourseService;
import com.bothash.crmbot.service.FacebookLeadConfigService;
import com.bothash.crmbot.service.GraphService;
import com.bothash.crmbot.service.PlatformService;
import com.bothash.crmbot.service.RoleModuleAccessService;
import com.bothash.crmbot.service.TargetMailsService;
import com.bothash.crmbot.service.UserMasterService;
import com.bothash.crmbot.service.impl.ModulesService;
import com.bothash.crmbot.spec.DashboardBasicResponseSorter;
import com.bothash.crmbot.spec.ExcelHelper;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.RoleRepresentation;

@Controller
@RequestMapping("/admin/")
public class AdminController {
	
	@Autowired
	private FacebookLeadConfigService facebookLeadConfigService;
	
	@Autowired
	private ActiveTaskService activeTaskService;
	
	@Autowired
	private AutomationUserService automationUserService;
	
	@Autowired
	private PlatformService platfromService;
	
	@Autowired
	private AutomationService automationService;
	
	@Autowired
	private CourseService courseService;
	
	@Autowired
	private UserMasterService userMasterService;
	
	@Autowired
	private GraphService graphService;
	
	@Autowired
	private ExcelHelper excelHelper;
	
	@Autowired
	private TargetMailsService targetMailsService;
	
	@Autowired
	private RestTemplate restTemplate;
	
	@Autowired
	private ModulesService modulesService;
	
	@Autowired
	private RoleModuleAccessService roleModuleAccessService;
	
	@Autowired
	private CommentMasterService commentMasterService;

	@Value("${crmbot-client-id}")
	private String crmbotClientId;
	
	@Value("${keycloak.auth-server-url}")
	private String keycloackUrl;
	
	@Value("${keycloack.admin.username}")
	private String adminUserName;
	
	@Value("${keycloack.admin.password}")
	private String adminPassword;
	
	@Value("${keycloak.credentials.secret}")
	private String keycloackClientSecret;
	
	@Value("${facebook.access.token}")
	private String facebookAccessToken;
	
	@Value("${keycloak.realm}")
	private String keycloackRealm;
	

	@Autowired
	private Keycloak keycloak;
	
	@GetMapping("dashboard")
	public ModelAndView dashboard(Principal principal) {
		
		KeycloakAuthenticationToken token = (KeycloakAuthenticationToken) principal;
		AccessToken accessToken = token.getAccount().getKeycloakSecurityContext().getToken();
		
		ModelAndView model=new ModelAndView();
		Set<String> roles=token.getAccount().getRoles();
		
		String role = roles.stream().findFirst().orElse(null);
		
		model.addObject("role", role);
		model.addObject("userName", accessToken.getName());
		model.setViewName("dashboard");
		model.addObject("dashboard", true);
		model.addObject("totalTask", activeTaskService.countOfTotalTask());
//		model.addObject("totalActiveTask", activeTaskService.countOfActiveTask());
		model.addObject("totalConvertedTask", activeTaskService.countOfConvertedTask(true));
		model.addObject("totalTodaysTask", activeTaskService.countOfTodaysTask());
		List<Platforms> platforms=platfromService.getAll();
//		List<Long> activeLeadsByPlatform=new ArrayList<Long>();
//		List<Long> convertedLeadsByPlatform=new ArrayList<Long>();
//		List<Long> notConvertedLeadsByPlatform=new ArrayList<Long>();
//		for(Platforms platform:platforms) {
//			activeLeadsByPlatform.add(this.activeTaskService.countOfTotalActiveTaskByPlatform(platform.getName()));
//			convertedLeadsByPlatform.add(this.activeTaskService.countOfTotalConvertedTaskByPlatform(platform.getName(),true));
//			notConvertedLeadsByPlatform.add(this.activeTaskService.countOfTotalConvertedTaskByPlatform(platform.getName(),false));
//		}
		
		List<Course> courses=this.courseService.getAll();
//		model.addObject("activeLeadsByPlatform", activeLeadsByPlatform);
//		model.addObject("convertedLeadsByPlatform", convertedLeadsByPlatform);
//		model.addObject("notConvertedLeadsByPlatform", notConvertedLeadsByPlatform);
		model.addObject("platforms", platforms);
		model.addObject("courses", courses);
		model.addObject("isAdmin", true);
		
		UserMaster userDetails=this.userMasterService.getByUserName(accessToken.getPreferredUsername());
		if(userDetails!=null)
			model.addObject("isUserActive", userDetails.getIsActive());
		else
			model.addObject("isUserActive", false);
		model.addObject("prefferedUserName", accessToken.getPreferredUsername());
		Boolean isAutomated=false;
		List<Automation> autoamtions=this.automationService.getByIsActive(true);
		if(autoamtions.size()>0) {
			isAutomated=true;
		}
		model.addObject("isAutomated", isAutomated);
		
		
		List<String> nextRoles=new ArrayList<>();
		nextRoles.add("manager");
		nextRoles.add("telecaller");
		nextRoles.add("counsellor");
		List<Object> nextUsers=new ArrayList<>();
		
		HttpHeaders header=new HttpHeaders();
		MultiValueMap<String,String> body= new  LinkedMultiValueMap<>();
		body.add("grant_type", "password");
		body.add("client_secret", keycloackClientSecret);
		body.add("username", adminUserName);
		body.add("password", adminPassword);
		body.add("client_id", "admin-cli");
		
		HttpEntity<MultiValueMap<String, String>> entity=new HttpEntity<>(body,header);
		
		HashMap response=restTemplate.postForObject(keycloackUrl+"/realms/master/protocol/openid-connect/token",entity, HashMap.class);
		String adminAccessToken=response.get("access_token").toString();
		
		HttpHeaders httpHeaders=new HttpHeaders();
		httpHeaders.set("Authorization", "Bearer "+adminAccessToken);
		
		for(String nextRole:nextRoles) {
			ResponseEntity<Object> userResponse=restTemplate.exchange(keycloackUrl+"/admin/realms/crmbot/clients/"+crmbotClientId+"/roles/"+nextRole+"/users",HttpMethod.GET,new HttpEntity<>(httpHeaders),Object.class);
			try {
				List<LinkedHashMap<String, Object>> userList = (List<LinkedHashMap<String, Object>>) userResponse.getBody();
				for(LinkedHashMap<String, Object> userMap:userList) {
					try {
						UserMaster user = this.userMasterService.getByUserName(userMap.get("username").toString());
						if(user!=null) {
							userMap.put("isActiveOnCRM", user.getIsActive());
						}
					}catch (Exception e) {
						e.printStackTrace();
						userMap.put("isActiveOnCRM", false);
					}
					
					
				}
				nextUsers.add(userResponse.getBody());
			}catch (Exception e) {
				e.printStackTrace();
			}
			
		}
		model.addObject("nextRoles", nextRoles);
		model.addObject("nextUsers", nextUsers);
		
		List<RoleModuleAccess> access = this.roleModuleAccessService.getByRole(role);
		model.addObject("access", access);
		
		return model;
	}
	
	@GetMapping("/add-management")
	public ModelAndView addManagement(Principal principal) {
		
		KeycloakAuthenticationToken token = (KeycloakAuthenticationToken) principal;
		AccessToken accessToken = token.getAccount().getKeycloakSecurityContext().getToken();
		
		ModelAndView model=new ModelAndView();
		List<FacebookLeadConfigs> facebookLeadConfigs= facebookLeadConfigService.getAll();
		model.setViewName("add-management");
		model.addObject("addManagement", true);
		model.addObject("facebookLeadConfigs", facebookLeadConfigs);
		model.addObject("isAdmin", true);
		
		Set<String> roles=token.getAccount().getRoles();
		String role = roles.stream().findFirst().orElse(null);
		
		UserMaster userDetails=this.userMasterService.getByUserName(accessToken.getPreferredUsername());
		if(userDetails!=null)
			model.addObject("isUserActive", userDetails.getIsActive());
		else
			model.addObject("isUserActive", true);
		model.addObject("prefferedUserName", accessToken.getPreferredUsername());
		Boolean isAutomated=false;
		List<Automation> autoamtions=this.automationService.getByIsActive(true);
		if(autoamtions.size()>0) {
			isAutomated=true;
		}
		
		model.addObject("isAutomated", isAutomated);
		model.addObject("role", role);
		model.addObject("userName", accessToken.getName());
		
		List<RoleModuleAccess> access = this.roleModuleAccessService.getByRole(role);
		model.addObject("access", access);
		
		return model;
	}
	
	@GetMapping("/add-management-table")
	public ModelAndView addManagementTable(Principal principal) {
		
		KeycloakAuthenticationToken token = (KeycloakAuthenticationToken) principal;
		AccessToken accessToken = token.getAccount().getKeycloakSecurityContext().getToken();
		Set<String> roles=token.getAccount().getRoles();
		String role = roles.stream().findFirst().orElse(null);
		
		
		ModelAndView model=new ModelAndView();
		List<FacebookLeadConfigs> facebookLeadConfigs= facebookLeadConfigService.getAll();
		model.setViewName("add-management-table");
		model.addObject("addManagement", true);
		model.addObject("facebookLeadConfigs", facebookLeadConfigs);
		model.addObject("isAdmin", true);
		model.addObject("userName", accessToken.getName());
		
		UserMaster userDetails=this.userMasterService.getByUserName(accessToken.getPreferredUsername());
		if(userDetails!=null)
			model.addObject("isUserActive", userDetails.getIsActive());
		else
			model.addObject("isUserActive", true);
		model.addObject("prefferedUserName", accessToken.getPreferredUsername());
		
		List<RoleModuleAccess> access = this.roleModuleAccessService.getByRole(role);
		model.addObject("access", access);
		return model;
	}
	
	
	@PostMapping("/savecampaign")
	public ModelAndView saveCampaign(@RequestBody FacebookLeadConfigs facebookLeadConfigs) {
		
		facebookLeadConfigs.setTimestamp(LocalDateTime.now().minusDays(100));
		facebookLeadConfigs.setSizeLimit(100000l);
		if(facebookLeadConfigs.getPlatform().equalsIgnoreCase(Constants.facebook)) {
			facebookLeadConfigs.setUrl("https://graph.facebook.com/v18.0/{lead_id}/leads");
			facebookLeadConfigs.setAccessToken(facebookAccessToken);
		}
		FacebookLeadConfigs existing=this.facebookLeadConfigService.getByCampaignName(facebookLeadConfigs.getCampaignName());
		if(existing!=null) {
			facebookLeadConfigs.setId(existing.getId());
		}
		FacebookLeadConfigs savedFacebookLeadConfigs=this.facebookLeadConfigService.save(facebookLeadConfigs);
		
		ModelAndView model=new ModelAndView();
		List<FacebookLeadConfigs> allfacebookLeadConfigs= facebookLeadConfigService.getAll();
		model.setViewName("add-management-table");
		model.addObject("addManagement", true);
		model.addObject("facebookLeadConfigs", allfacebookLeadConfigs);
		model.addObject("isAdmin", true);
		return model;
		
	}
	
	@PutMapping("/graphs/get")
	public ModelAndView graphs(@RequestBody FilterRequests filterRequests,@RequestParam Boolean isAsc,@RequestParam String paramter){
		//List<ActiveTask> tasks=this.activeTaskService.getGraphs(filterRequests);
		ModelAndView model = new ModelAndView();
		if(filterRequests.getRole()==null || filterRequests.getRole()=="") {
			filterRequests.setRole("telecaller");
		}
		filterRequests.setIsDashboardFilter(true);
		List<DashboardBasicResponse> dashboardResponse = this.activeTaskService.countBySpecification(filterRequests,false);
//		DashboardBasicResponseSorter.sortDashboardList(dashboardResponse, paramter, isAsc);
		for(DashboardBasicResponse dasBasic : dashboardResponse) {
			UserMaster userMaster =this.userMasterService.getByUserName(dasBasic.getUserId());
			if(userMaster!=null ) {
				if (userMaster.getImage() != null) {
					dasBasic.setImage(Base64.getEncoder().encodeToString(userMaster.getImage()));
				}
			}
		}
		model.addObject("dashboardResponseList", dashboardResponse);
		model.setViewName("dashboardBasicTable");
		return model;
	}
	
	@PutMapping("/graphs/get-scrutiny")
	public ModelAndView graphsScrutiny(@RequestBody FilterRequests filterRequests,@RequestParam Boolean isAsc,@RequestParam String paramter){
		//List<ActiveTask> tasks=this.activeTaskService.getGraphs(filterRequests);
		ModelAndView model = new ModelAndView();
		if(filterRequests.getRole()==null || filterRequests.getRole()=="") {
			filterRequests.setRole("telecaller");
		}
		filterRequests.setIsDashboardFilter(true);
		List<DashboardBasicResponse> dashboardResponse = this.activeTaskService.countBySpecification(filterRequests,true);
//		DashboardBasicResponseSorter.sortDashboardList(dashboardResponse, paramter, isAsc);
		for(DashboardBasicResponse dasBasic : dashboardResponse) {
			UserMaster userMaster =this.userMasterService.getByUserName(dasBasic.getUserId());
			if(userMaster!=null ) {
				if (userMaster.getImage() != null) {
					dasBasic.setImage(Base64.getEncoder().encodeToString(userMaster.getImage()));
				}
			}
		}
		model.addObject("dashboardResponseList", dashboardResponse);
		model.setViewName("dashboardScrutinyTable");
		return model;
	}
	
	@PutMapping("/graphs/carddata")
	public ResponseEntity<DashboardCardData> cardData(@RequestBody FilterRequests filterRequests){
		DashboardCardData dashboardResponse = this.activeTaskService.getDashBoardCardData(filterRequests);
		return new ResponseEntity<DashboardCardData>(dashboardResponse, HttpStatus.OK);
	}
	
	@PutMapping("/graphreport")
	@ResponseBody
	public Callable<ResponseEntity<Resource>> report(@RequestBody FilterRequests filterRequests){
		filterRequests.setIsDashboardFilter(true);
		List<ActiveTask> tasks=this.activeTaskService.getGraphs(filterRequests);
		filterRequests.setIsActive(false);
		filterRequests.setIsConverted(true);
		if(filterRequests.getIsDateTypeChanged()) {
			filterRequests.setDateType("admissionDate");
		}
		tasks.addAll(this.activeTaskService.getGraphs(filterRequests));
		
		try {
			String fileName="test.xlsx";
			ByteArrayInputStream in=excelHelper.taskToExcel(tasks);
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
	
	@GetMapping("mail-configuration")
	public ModelAndView mailConfiguration(Principal principal) {
		
		KeycloakAuthenticationToken token = (KeycloakAuthenticationToken) principal;
		AccessToken accessToken = token.getAccount().getKeycloakSecurityContext().getToken();
		
		ModelAndView model=new ModelAndView();
		model.addObject("userName", accessToken.getName());
		model.addObject("mailConfiguration", true);
		model.setViewName("mail-configuration");
		model.addObject("isAdmin", true);
		
		Set<String> roles=token.getAccount().getRoles();
		String role = roles.stream().findFirst().orElse(null);
		
		model.addObject("role", role);
		
		
		UserMaster userDetails=this.userMasterService.getByUserName(accessToken.getPreferredUsername());
		if(userDetails!=null)
			model.addObject("isUserActive", userDetails.getIsActive());
		else
			model.addObject("isUserActive", false);
		model.addObject("prefferedUserName", accessToken.getPreferredUsername());
		Boolean isAutomated=false;
		List<Automation> autoamtions=this.automationService.getByIsActive(true);
		if(autoamtions.size()>0) {
			isAutomated=true;
		}
		model.addObject("isAutomated", isAutomated);
		
		List<RoleModuleAccess> access = this.roleModuleAccessService.getByRole(role);
		model.addObject("access", access);
		
		return model;
	}
	
	@GetMapping("mail-configuration-table")
	public ModelAndView mailConfigTable(Principal principal) {
		
		KeycloakAuthenticationToken token = (KeycloakAuthenticationToken) principal;
		AccessToken accessToken = token.getAccount().getKeycloakSecurityContext().getToken();
		Set<String> roles=token.getAccount().getRoles();
		String role = roles.stream().findFirst().orElse(null);
		
		ModelAndView model=new ModelAndView();
		
		List<TargetMails> allMails=targetMailsService.getAll();
		model.addObject("allMails", allMails);
		model.setViewName("mail-configuration-table");
		
		List<RoleModuleAccess> access = this.roleModuleAccessService.getByRole(role);
		model.addObject("access", access);
		return model;
	}
	
	@PutMapping("/savemailConfig")
	@ResponseBody
	public ResponseEntity<TargetMails> saveMailConfig(@RequestBody TargetMails targetMails){
		TargetMails savedtargetMails=targetMailsService.save(targetMails);
		return new  ResponseEntity<TargetMails>(savedtargetMails,HttpStatus.OK);
	}
	
	@GetMapping("/coursemaster")
	public ModelAndView courseMaster(Principal principal) {
		
		KeycloakAuthenticationToken token = (KeycloakAuthenticationToken) principal;
		AccessToken accessToken = token.getAccount().getKeycloakSecurityContext().getToken();
		
		ModelAndView model=new ModelAndView();
		

		Set<String> roles=token.getAccount().getRoles();
		String role = roles.stream().findFirst().orElse(null);
		model.addObject("role", role);
		
		model.addObject("userName", accessToken.getName());
		model.addObject("courseMaster", true);
		model.setViewName("course-master");
		model.addObject("isAdmin", true);
		
		
		
		UserMaster userDetails=this.userMasterService.getByUserName(accessToken.getPreferredUsername());
		if(userDetails!=null)
			model.addObject("isUserActive", userDetails.getIsActive());
		else
			model.addObject("isUserActive", false);
		model.addObject("prefferedUserName", accessToken.getPreferredUsername());
		Boolean isAutomated=false;
		List<Automation> autoamtions=this.automationService.getByIsActive(true);
		if(autoamtions.size()>0) {
			isAutomated=true;
		}
		model.addObject("isAutomated", isAutomated);
		List<Course> courses=this.courseService.getAll();
        model.addObject("courses",courses);
        
        List<RoleModuleAccess> access = this.roleModuleAccessService.getByRole(role);
		model.addObject("access", access);
		return model;
	}
	
	@GetMapping("/sourcemaster")
	public ModelAndView sourceMaster(Principal principal) {
		
		KeycloakAuthenticationToken token = (KeycloakAuthenticationToken) principal;
		AccessToken accessToken = token.getAccount().getKeycloakSecurityContext().getToken();
		
		ModelAndView model=new ModelAndView();

		Set<String> roles=token.getAccount().getRoles();
		String role = roles.stream().findFirst().orElse(null);
		
		model.addObject("role", role);
		model.addObject("userName", accessToken.getName());
		model.addObject("sourceMaster", true);
		model.setViewName("source-master");
		model.addObject("isAdmin", true);
		
		
		UserMaster userDetails=this.userMasterService.getByUserName(accessToken.getPreferredUsername());
		if(userDetails!=null)
			model.addObject("isUserActive", userDetails.getIsActive());
		else
			model.addObject("isUserActive", false);
		model.addObject("prefferedUserName", accessToken.getPreferredUsername());
		Boolean isAutomated=false;
		List<Automation> autoamtions=this.automationService.getByIsActive(true);
		if(autoamtions.size()>0) {
			isAutomated=true;
		}
		model.addObject("isAutomated", isAutomated);
		List<Platforms> sources=this.platfromService.getAll();
        model.addObject("sources",sources);
        
        List<RoleModuleAccess> access = this.roleModuleAccessService.getByRole(role);
		model.addObject("access", access);
		return model;
	}
	
	@GetMapping("/commentmaster")
	public ModelAndView commentMaster(Principal principal) {
		
		KeycloakAuthenticationToken token = (KeycloakAuthenticationToken) principal;
		AccessToken accessToken = token.getAccount().getKeycloakSecurityContext().getToken();
		
		ModelAndView model=new ModelAndView();

		Set<String> roles=token.getAccount().getRoles();
		String role = roles.stream().findFirst().orElse(null);
		
		model.addObject("role", role);
		model.addObject("userName", accessToken.getName());
		model.addObject("commentMaster", true);
		model.setViewName("comment-master");
		
		
		UserMaster userDetails=this.userMasterService.getByUserName(accessToken.getPreferredUsername());
		if(userDetails!=null)
			model.addObject("isUserActive", userDetails.getIsActive());
		else
			model.addObject("isUserActive", false);
		model.addObject("prefferedUserName", accessToken.getPreferredUsername());
		Boolean isAutomated=false;
		List<Automation> autoamtions=this.automationService.getByIsActive(true);
		if(autoamtions.size()>0) {
			isAutomated=true;
		}
		model.addObject("isAutomated", isAutomated);
		List<CommentMaster> comments = this.commentMasterService.getAllComments();
        model.addObject("comments",comments);
        
        List<RoleModuleAccess> access = this.roleModuleAccessService.getByRole(role);
		model.addObject("access", access);
		return model;
	}
	
	@GetMapping("/accessManagement")
	public ModelAndView getMethodName(Principal principal) {
		
		KeycloakAuthenticationToken token = (KeycloakAuthenticationToken) principal;
		AccessToken accessToken = token.getAccount().getKeycloakSecurityContext().getToken();
		
		ModelAndView model = new ModelAndView();
		
	
		Set<String> roles=token.getAccount().getRoles();
		String role = roles.stream().findFirst().orElse(null);
		
		model.addObject("role", role);
		model.addObject("userName", accessToken.getName());
		model.setViewName("access-management");
		model.addObject("isAdmin", true);
		model.addObject("accessManagement", true);
		
		
		UserMaster userDetails=this.userMasterService.getByUserName(accessToken.getPreferredUsername());
		if(userDetails!=null)
			model.addObject("isUserActive", userDetails.getIsActive());
		else
			model.addObject("isUserActive", false);
		model.addObject("prefferedUserName", accessToken.getPreferredUsername());
		Boolean isAutomated=false;
		List<Automation> autoamtions=this.automationService.getByIsActive(true);
		if(autoamtions.size()>0) {
			isAutomated=true;
		}
		model.addObject("isAutomated", isAutomated);
		
		List<RoleRepresentation> clientRoles = keycloak.realm(keycloackRealm).clients().get(crmbotClientId).roles().list();
		model.addObject("roles", clientRoles);
		
		List<Modules> modules=this.modulesService.getAll();
		model.addObject("modules", modules);
		
		List<RoleModuleAccess> access = this.roleModuleAccessService.getByRole(role);
		model.addObject("access", access);
		return model;
	}
	
	
}
