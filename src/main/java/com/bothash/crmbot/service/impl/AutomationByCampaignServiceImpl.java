package com.bothash.crmbot.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

import com.bothash.crmbot.entity.AutomationByCampaign;
import com.bothash.crmbot.entity.AutomationByCourse;
import com.bothash.crmbot.entity.Course;
import com.bothash.crmbot.entity.FacebookLeadConfigs;
import com.bothash.crmbot.entity.LastAllocatedUser;
import com.bothash.crmbot.entity.UserMaster;
import com.bothash.crmbot.repository.AutomationByCampaignRepository;
import com.bothash.crmbot.service.AutomationByCampaignService;
import com.bothash.crmbot.service.FacebookLeadConfigService;
import com.bothash.crmbot.service.LastAllocatedUserService;
import com.bothash.crmbot.service.UserMasterService;

@Service
public class AutomationByCampaignServiceImpl implements AutomationByCampaignService{

	@Autowired
	private AutomationByCampaignRepository automationByCampaignRepository;
	
	@Autowired
	private FacebookLeadConfigService facebookLeadConfigService;
	
	@Autowired
	private LastAllocatedUserService lastAllocatedUserService;
	
	@Autowired
	private UserMasterService userMasterService;
	
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
	
	@Override
	public AutomationByCampaign getByCampaignIdAndGroupId(Long campaignId, String groupId) {
		return automationByCampaignRepository.findByFacebookLeadConfigsIdAndGroupId(campaignId,groupId);
	}

	@Override
	public AutomationByCampaign save(AutomationByCampaign automationByCampaign) {
		return automationByCampaignRepository.save(automationByCampaign);
	}

	@Override
	public List<AutomationByCampaign> getByCampaignId(Long id) {
		return automationByCampaignRepository.findByFacebookLeadConfigsId(id);
	}
	
	@Override
	public  Map<String,String> allocate(String campaignName) {
		FacebookLeadConfigs facebookLeadConfigs = facebookLeadConfigService.getByCampaignName(campaignName);
		
		
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
		String manager="";
		List<LinkedHashMap> telecaller=new ArrayList<>();
		LastAllocatedUser lastAllocatedUser=this.lastAllocatedUserService.getFirst();
		Boolean lastAllocatedUserFound=false;
		Boolean userAllocated=false;
		Map<String,String> responseList =new HashMap<String,String>();
		try {
			List<AutomationByCampaign> groups=this.automationByCampaignRepository.findByIsActiveAndFacebookLeadConfigsId(true, facebookLeadConfigs.getId());
			for(AutomationByCampaign group:groups) {
				if(group.getGroupId()!=null && !userAllocated) {
					
					try {
						ResponseEntity<List> userResponse=restTemplate.exchange(keycloackUrl+"/admin/realms/crmbot/groups/"+group.getGroupId()+"/members",HttpMethod.GET,new HttpEntity<>(httpHeaders),List.class);
						
						System.out.println(userResponse.getBody().toString());
						@SuppressWarnings("unchecked")
						List<LinkedHashMap> users=userResponse.getBody();
						for(int i=0;i<users.size();i++) {
							LinkedHashMap user=users.get(i);
							
							UserMaster existing=userMasterService.getByUserName(user.get("username").toString());
							
							
							ResponseEntity<List> rolesResponse=this.restTemplate.exchange(keycloackUrl+"/admin/realms/crmbot/users/"+user.get("id")+"/role-mappings/clients/"+crmbotClientId,HttpMethod.GET,new HttpEntity<>(httpHeaders),List.class);
							
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
									}else if(!userAllocated && (existing==null || existing.getIsActive())){
										
										responseList.put("userEmail", user.get("username").toString());
										responseList.put("userName",user.get("firstName").toString()+user.get("lastName").toString());
										userAllocated=true;
										LastAllocatedUser lasUserAllocatedNew=new LastAllocatedUser();
										lasUserAllocatedNew.setUserId(user.get("email").toString());
										LastAllocatedUser savedUser=this.lastAllocatedUserService.save(lasUserAllocatedNew);
									}if(i==users.size()-1 && !userAllocated) {
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
		}catch(Exception e) {
			
		}
		
		
		return responseList;
		
	}

	@Override
	public Long delete(Long id) {
		Long response=1l;
		AutomationByCampaign automation=automationByCampaignRepository.findById(id).get();
		response=automation.getFacebookLeadConfigs().getId();
		automationByCampaignRepository.deleteById(id);
		return response;
	}

}
