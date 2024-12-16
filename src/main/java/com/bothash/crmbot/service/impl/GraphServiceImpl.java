package com.bothash.crmbot.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.bothash.crmbot.dto.FilterRequests;
import com.bothash.crmbot.entity.ActiveTask;
import com.bothash.crmbot.entity.Course;
import com.bothash.crmbot.entity.Platforms;
import com.bothash.crmbot.service.ActiveTaskService;
import com.bothash.crmbot.service.CourseService;
import com.bothash.crmbot.service.GraphService;
import com.bothash.crmbot.service.PlatformService;

@Service
public class GraphServiceImpl implements GraphService{

	@Autowired
	private ActiveTaskService activeTaskService;
	
	@Autowired
	private PlatformService platformService;
	
	@Autowired
	private CourseService courseService;
	
	@Autowired
	private RestTemplate restTemplate;

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
	
	@Override
	public Map<String, List> generateXAndYPlots(List<ActiveTask> tasks, FilterRequests filterRequests) {
		Map<String, List> reponse=new HashMap<>();
		List<Integer> YPlots=new ArrayList<>();
		List<String> XPlots = new ArrayList<>();
		if(filterRequests.getRole()!=null && filterRequests.getRole().length()>1) {
			if(filterRequests.getUserName()==null || filterRequests.getUserName().length()<1) {
				
				reponse=userXPlot(tasks,filterRequests);
				
			}else if(filterRequests.getLeadPlatform()==null || filterRequests.getLeadPlatform().length()<1) {
				List<Platforms> platforms=platformService.getAll();
				for(Platforms platform:platforms) {
					XPlots.add(platform.getName());
					filterRequests.setLeadPlatform(platform.getName());
					List<ActiveTask> currUserTask=this.activeTaskService.getGraphs(filterRequests);
					YPlots.add(currUserTask.size());
				}
				reponse.put("XPlots", XPlots);
				reponse.put("YPlots", YPlots);
				
			}else if(filterRequests.getCourseName()==null || filterRequests.getCourseName().length()<1) {
				List<Course> courses=courseService.getAll();
				for(Course course:courses) {
					XPlots.add(course.getCourseName());
					filterRequests.setCourseName(course.getCourseName());
					List<ActiveTask> currUserTask=this.activeTaskService.getGraphs(filterRequests);
					YPlots.add(currUserTask.size());
				}
				reponse.put("XPlots", XPlots);
				reponse.put("YPlots", YPlots);
				
			}else if(filterRequests.getStatus()==null || filterRequests.getStatus().length()<1) {
				XPlots.add("open");
				XPlots.add("processed");
				XPlots.add("completed");
				XPlots.add("closed");
				for(String xplot:XPlots) {
					filterRequests.setStatus(xplot);
					List<ActiveTask> currUserTask=this.activeTaskService.getGraphs(filterRequests);
					YPlots.add(currUserTask.size());
				}
				reponse.put("XPlots", XPlots);
				reponse.put("YPlots", YPlots);
				
			}else if(filterRequests.getIsScheduled()==null ) {
				XPlots.add("Scheduled");
				XPlots.add("Not Scheduled");
				for(int i=0;i<2;i++) {
					if(i == 0)
						filterRequests.setIsScheduled(true);
					else
						filterRequests.setIsScheduled(false);
					List<ActiveTask> currUserTask=this.activeTaskService.getGraphs(filterRequests);
					YPlots.add(currUserTask.size());
				}
				reponse.put("XPlots", XPlots);
				reponse.put("YPlots", YPlots);
				
			}else if(filterRequests.getIsCounselled()==null ) {
				XPlots.add("Counselled");
				XPlots.add("Not Counselled");
				for(int i=0;i<2;i++) {
					if(i == 0)
						filterRequests.setIsCounselled(true);
					else
						filterRequests.setIsCounselled(false);
					List<ActiveTask> currUserTask=this.activeTaskService.getGraphs(filterRequests);
					YPlots.add(currUserTask.size());
				}
				reponse.put("XPlots", XPlots);
				reponse.put("YPlots", YPlots);
				
			}else {
				XPlots.add(filterRequests.getUserName());
				YPlots.add(tasks.size());
				reponse.put("XPlots", XPlots);
				reponse.put("YPlots", YPlots);
			}
			
		}else {
			XPlots.add("admin");
			XPlots.add("manager");
			XPlots.add("telecaller");
			XPlots.add("counsellor");
			
			for(String xplot:XPlots) {
				filterRequests.setRole(xplot);
				filterRequests.setAssignee(xplot);
				List<ActiveTask> currUserTask=this.activeTaskService.getGraphs(filterRequests);
				YPlots.add(currUserTask.size());
			}
			reponse.put("XPlots", XPlots);
			reponse.put("YPlots", YPlots);
		}
		return reponse;
	}
	
	
	private Map<String, List> userXPlot(List<ActiveTask> tasks, FilterRequests filterRequests){
		Map<String, List> mapResponse=new HashMap<>();
		List<Integer> YPlots=new ArrayList<>();
		if(filterRequests.getRole()!=null) {
			if(filterRequests.getUserName()==null || filterRequests.getUserName().length()<1) {
				
				List<String> nextUsers=new ArrayList<>();
				
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
				
				ResponseEntity<Object> userResponse=restTemplate.exchange(keycloackUrl+"/admin/realms/crmbot/clients/"+crmbotClientId+"/roles/"+filterRequests.getRole()+"/users",HttpMethod.GET,new HttpEntity<>(httpHeaders),Object.class);
				
				if(userResponse.getBody()!=null) {
					try {
						List<LinkedHashMap<String, Object>> userArray=(List<LinkedHashMap<String, Object>>) userResponse.getBody();
						for(int i=0;i<userArray.size();i++) {
							try {
								LinkedHashMap<String, Object> userObject=userArray.get(i);
								String userName="";
								if(userObject.containsKey("firstName")) {
									userName = userObject.get("firstName").toString()+" ";
								}
								if(userObject.containsKey("lastName")) {
									try {
										userName += userObject.get("lastName").toString().substring(0, 1);
									}catch (Exception e) {
										e.printStackTrace();
									}
								}
								nextUsers.add(userName);
								filterRequests.setUserName(userObject.get("username").toString());
								
								List<ActiveTask> currUserTask=this.activeTaskService.getGraphs(filterRequests);
								YPlots.add(currUserTask.size());
							}catch(Exception e2) {
								e2.printStackTrace();
							}
						}
						mapResponse.put("XPlots", nextUsers);
						mapResponse.put("YPlots", YPlots);
					}catch(Exception e) {
						e.printStackTrace();
					}
				}
				
			}
		}
		return mapResponse;
	}

}
