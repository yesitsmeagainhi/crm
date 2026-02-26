package com.bothash.crmbot.controller;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.Principal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import javax.annotation.security.RolesAllowed;

import org.apache.http.HttpResponse;
import org.jose4j.lang.JoseException;
import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.bothash.crmbot.dto.Constants;
import com.bothash.crmbot.entity.ActiveTask;
import com.bothash.crmbot.entity.Automation;
import com.bothash.crmbot.entity.DuplicateDetails;
import com.bothash.crmbot.entity.FacebookLeadConfigs;
import com.bothash.crmbot.entity.FacebookLeads;
import com.bothash.crmbot.entity.HistoryEvents;
import com.bothash.crmbot.entity.PushSubscription;
import com.bothash.crmbot.repository.SubscriptionRepository;
import com.bothash.crmbot.service.ActiveTaskService;
import com.bothash.crmbot.service.AutomationByCourseService;
import com.bothash.crmbot.service.AutomationBySourceService;
import com.bothash.crmbot.service.AutomationService;
import com.bothash.crmbot.service.DuplicateDetailsService;
import com.bothash.crmbot.service.FacebookLeadConfigService;
import com.bothash.crmbot.service.FacebookLeadsService;
import com.bothash.crmbot.service.HistoryEventsService;
import com.bothash.crmbot.service.LeadScoringService;
import com.bothash.crmbot.service.impl.TaskListener;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;
import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushService;
import nl.martijndwars.webpush.Subscription;
import nl.martijndwars.webpush.Subscription.Keys;

@RestController
@RequestMapping("/crmbot/facebook")
@Slf4j
public class FaceBookLeadsController {
	
	@Autowired
	private RestTemplate restTemplate;
	
	@Autowired
	private FacebookLeadConfigService facebookLeadConfigService;
	
	@Autowired
	private FacebookLeadsService facebookLeadsService;
	
	@Autowired
	private ActiveTaskService activeTaskService;
	
	@Autowired
	private HistoryEventsService historyEventsService;
	
	@Autowired
	private AutomationService automationService;
	
	@Autowired
	private AutomationByCourseService automationByCourseService;
	
	@Autowired
	private AutomationBySourceService automationBySourceService;
	
	@Autowired
	private TaskListener taskListener;
	
	@Autowired
	private DuplicateDetailsService duplicateDetailsService;
	
	@Autowired
	private  SubscriptionRepository subs;

	@Autowired
	private  PushService push;

	@Autowired
	private LeadScoringService leadScoringService;
	
	
	@Value("${facebook.access.token}")
	private String facebookAccessToken;
	
	@GetMapping("/leads")
	@Scheduled(fixedRate = 120000)
	public ResponseEntity<List<FacebookLeads>> getLeads(){


		List<FacebookLeadConfigs> activeConfigs=facebookLeadConfigService.getAllActiveCongifs("Facebook");
		log.info("Facebook scheduler fired. Active configs found: {}", activeConfigs.size());
		String url="";
		List<FacebookLeads> response=new ArrayList<>();
		for(FacebookLeadConfigs activeConfig:activeConfigs) {
			try {
				long timeInSeconds = activeConfig.getTimestamp().atZone(java.time.ZoneId.systemDefault()).toEpochSecond();
				log.info("Config '{}': DB timestamp={}, epoch filter={}", activeConfig.getCampaignName(), activeConfig.getTimestamp(), timeInSeconds);
				List<FacebookLeads> leadsToSave=new ArrayList<>();
				url=activeConfig.getUrl().replace("{lead_id}", activeConfig.getLeadId());

				String qrey="[{\"field\": \"time_created\",\"operator\": \"GREATER_THAN_OR_EQUAL\", \"value\": "+timeInSeconds+"  }]";

				url+="?access_token="+facebookAccessToken+"&fields=created_time,id,ad_id,form_id,field_data&filtering={qrey}&limit=1000000";
			
				@SuppressWarnings("unchecked")
				LinkedHashMap<String, Object> res=this.restTemplate.getForObject(url,LinkedHashMap.class,qrey);
				
				
				
				@SuppressWarnings("unchecked")
				List<LinkedHashMap<String, Object>> leadsResponses=(List<LinkedHashMap<String, Object>>) res.get("data");
				log.info("Facebook API returned {} leads for config: {}", leadsResponses != null ? leadsResponses.size() : 0, activeConfig.getCampaignName());

				for(int i=0;i<leadsResponses.size();i++) {
					try {
						LinkedHashMap<String, Object> leadResponse=leadsResponses.get(i);
						FacebookLeads leadToSave=new FacebookLeads();
						//leadToSave.setAdId(leadResponse.get("ad_id").toString());
						leadToSave.setFieldData(objectToJson(leadResponse.get("field_data")).toString());
						leadToSave.setFormId(leadResponse.get("form_id").toString());
						leadToSave.setLeadCreationTime(leadResponse.get("created_time").toString());
						leadToSave.setLeadId(leadResponse.get("id").toString());
						leadsToSave.add(leadToSave);
						
					}catch(Exception e) {
						e.printStackTrace();
					}
				}
				response.addAll(leadsToSave);
				activeConfig.setTimestamp(LocalDateTime.now());
				List<FacebookLeads> savedLeads=this.facebookLeadsService.saveAll(leadsToSave);
				List<ActiveTask> taskToSave=new ArrayList<>();
				for(FacebookLeads facbooklead:savedLeads) {
					ActiveTask activeTask=new ActiveTask();
					activeTask.setIsActive(true);
					activeTask.setAssignee("admin");
					activeTask.setFacebookLeads(facbooklead);
					activeTask.setIsClaimed(false);
					activeTask.setLeadPlatform("F");
					activeTask.setTaskGroup("Admin");
					activeTask.setAssignedTime(LocalDateTime.now().plusHours(5).plusMinutes(30));
					activeTask.setTaskName("");
					activeTask.setStatus("Open");
					activeTask.setCampaign(activeConfig.getCampaignName());
					String fieldData=activeTask.getFacebookLeads().getFieldData();
					String name="";
					String phoneNumber="";
					String email="";
					JSONArray fieldDataArray=new JSONArray(fieldData);
					for(int i=0;i<fieldDataArray.length();i++) {
						try {
							if(fieldDataArray.getJSONObject(i).getString("name").equals("name")) {
								name=fieldDataArray.getJSONObject(i).getJSONArray("values").get(0).toString();
							}else if(fieldDataArray.getJSONObject(i).getString("name").equals("full_name")) {
								name=fieldDataArray.getJSONObject(i).getJSONArray("values").get(0).toString();
							}else if(fieldDataArray.getJSONObject(i).getString("name").equals("first_name")) {
								name=fieldDataArray.getJSONObject(i).getJSONArray("values").get(0).toString();
							}
							activeTask.setLeadName(name);
						}catch(Exception e2) {
							e2.printStackTrace();
							activeTask.setLeadName("");
						}
						try {
							if(fieldDataArray.getJSONObject(i).getString("name").equals("phonenumber")) {
								phoneNumber=fieldDataArray.getJSONObject(i).getJSONArray("values").get(0).toString();
							}else if(fieldDataArray.getJSONObject(i).getString("name").equals("number")) {
								phoneNumber=fieldDataArray.getJSONObject(i).getJSONArray("values").get(0).toString();
							}else if(fieldDataArray.getJSONObject(i).getString("name").equals("phone_number") || fieldDataArray.getJSONObject(i).getString("name").equals("contact_number") || fieldDataArray.getJSONObject(i).getString("name").contains("contact")) {
								phoneNumber=fieldDataArray.getJSONObject(i).getJSONArray("values").get(0).toString();
							}
							else if(fieldDataArray.getJSONObject(i).getString("name").equals("phone") || fieldDataArray.getJSONObject(i).getString("name").equals("contact_number") || fieldDataArray.getJSONObject(i).getString("name").contains("contact")) {
								phoneNumber=fieldDataArray.getJSONObject(i).getJSONArray("values").get(0).toString();
							}
							phoneNumber=phoneNumber.replaceAll("\\+91", "");
							phoneNumber=phoneNumber.replaceAll(" ", "");
							activeTask.setPhoneNumber(phoneNumber);
							
						}catch(Exception e2) {
							e2.printStackTrace();
							activeTask.setPhoneNumber("");
						}
						try {
							if(fieldDataArray.getJSONObject(i).getString("name").equals("email")) {
								email=fieldDataArray.getJSONObject(i).getJSONArray("values").get(0).toString();
							}else if(fieldDataArray.getJSONObject(i).getString("name").equals("email")) {
								email=fieldDataArray.getJSONObject(i).getJSONArray("values").get(0).toString();
							}
							email=email.replaceAll("\\+91", "");
							email=email.replaceAll(" ", "");
							//activeTask.setPhoneNumber(phoneNumber);
							
						}catch(Exception e2) {
							e2.printStackTrace();
							activeTask.setPhoneNumber("");
						}
					}
					activeTask.setLeadName(name);
					boolean isDuplicate = false;
					List<ActiveTask> existingTask =this.activeTaskService.getTaskByPhoneNumber(activeTask.getPhoneNumber());
					if(existingTask!=null && existingTask.size()>0 && activeTask.getPhoneNumber()!=null && !activeTask.getPhoneNumber().isEmpty()) {
						isDuplicate = true;
						activeTask.setIsDuplicate(true);
						try {
							DuplicateDetails duplicateDetails = new DuplicateDetails();
				    		duplicateDetails.setActiveTask(existingTask.get(0));
				    		duplicateDetails.setPlatform("G");
				    		
				    		this.duplicateDetailsService.save(duplicateDetails);
				    		this.facebookLeadConfigService.save(activeConfig);
				    		HistoryEvents event =new HistoryEvents();
							event.setActiveTask(existingTask.get(0));
							event.setUserEmail("Facebook");
							event.setUserName("Facebook");
							event.setUserId("Facebook");
							event.setEvent("Duplicate task created through Facebook");
							historyEventsService.save(event);
							try {
								PushSubscription s =subs.findByUserName(existingTask.get(0).getOwner());
								String payload = "{"
								        + "\"title\": \"🎉 New Admission Lead!\","
								        + "\"body\": \"New lead is added with number "+activeTask.getPhoneNumber()+". Tap to review.\","
								        + "\"icon\": \"https://www.vmedify.com/img/logos/crmb-logo.jpg\","
								        + "\"url\": \"https://www.vmedify.com\""
								        + "}";
							    try {
							    	Keys k = new Keys(s.getP256dh(), s.getAuth());
						    	  	Subscription sub = new Subscription(s.getEndpoint(), k);
						        
							        HttpResponse notiResponse = push.send(new Notification(sub,payload));
							        log.info("pushed");
							    } catch ( IOException | GeneralSecurityException | JoseException | ExecutionException | InterruptedException ex) {
							        // 404 / 410 ⇒ subscription no longer valid – prune it
							    	log.error(ex.getMessage());
//							        if (ex.getMessage().contains("410") || ex.getMessage().contains("404"))
//							          subs.delete(s);
							      }
							} catch (Exception e) {
								log.error("uanble to send notification");
							}
							
							
				    		break;
						} catch (Exception e) {
							e.printStackTrace();
						}
			    		try {
			    			activeTask.setAssignee(existingTask.get(0).getAssignee());
							activeTask.setOwner(existingTask.get(0).getOwner());
							activeTask.setStatus(existingTask.get(0).getStatus());
							activeTask.setManagerName(existingTask.get(0).getManagerName());
							activeTask.setTelecallerName(existingTask.get(0).getTelecallerName());
							activeTask.setCounsellorName(existingTask.get(0).getCounsellorName());
			    		}catch(Exception e) {
			    			e.printStackTrace();
			    		}
					}
					// Auto-classify lead type using scoring engine
					String autoLeadType = leadScoringService.classifyLead(name, phoneNumber, email, isDuplicate);
					activeTask.setLeadType(autoLeadType);

					if(activeTask.getAssignee()==null || activeTask.getAssignee().equals("admin")) {
						List<Automation> automationList=automationService.getByIsActive(true);
						if(automationList.size()>0) {
							Automation automationParamter=automationList.get(0);
							Map<String,String> userToAllocate =new HashMap<String,String>();
							if(automationParamter.getParamter().equals("Source")) {
								userToAllocate=this.automationBySourceService.allocate(activeTask.getLeadPlatform());
								
							}else if(automationParamter.getParamter().equals("Cousre")) {
								userToAllocate=this.automationByCourseService.allocate(activeTask.getCourse());
								
							}else if(automationParamter.getParamter().equals("Random")) {
								userToAllocate=this.activeTaskService.randomlyAssgin(activeTask, "admin");
							
							}
							try {
								if(userToAllocate!=null && userToAllocate.size()>0) {
									activeTask.setAssignee("telecaller");
									activeTask.setIsClaimed(false);
									try {
										if(userToAllocate.containsKey("managerName"))
											activeTask.setManagerName(userToAllocate.get("managerName"));
									}catch(Exception e3) {
										e3.printStackTrace();
									}
									activeTask.setStatus("Assigned to "+userToAllocate.get("userName"));
									activeTask.setAssignedTime(LocalDateTime.now());
									activeTask.setTelecallerName(userToAllocate.get("userEmail"));
									activeTask.setOwner(userToAllocate.get("userEmail"));
								}
							}catch(Exception e) {
								e.printStackTrace();
							}
							
							
						}
					}
					
					try {
						taskListener.sendTicketCreationMai(activeTask.getCampaign(), email);
					}catch(Exception e2) {
						e2.printStackTrace();
					}
					taskToSave.add(activeTask);
					
				}
				
				List<ActiveTask> savedTask=this.activeTaskService.saveAll(taskToSave);
				
				
				
				for(ActiveTask task:savedTask) {
					HistoryEvents event =new HistoryEvents();
					event.setActiveTask(task);
					event.setEvent("Task created");
					historyEventsService.save(event);
					try {
						PushSubscription s =subs.findByUserName(task.getOwner());
						String payload = "{"
						        + "\"title\": \"🎉 New Admission Lead!\","
						        + "\"body\": \"New lead is added with number "+task.getPhoneNumber()+". Tap to review.\","
						        + "\"icon\": \"https://www.vmedify.com/img/logos/crmb-logo.jpg\","
						        + "\"url\": \"https://www.vmedify.com\""
						        + "}";
					    try {
					    	Keys k = new Keys(s.getP256dh(), s.getAuth());
				    	  	Subscription sub = new Subscription(s.getEndpoint(), k);
				        
					        HttpResponse notiResponse = push.send(new Notification(sub,payload));
					        log.info("pushed");
					    } catch ( IOException | GeneralSecurityException | JoseException | ExecutionException | InterruptedException ex) {
					        // 404 / 410 ⇒ subscription no longer valid – prune it
					    	log.error(ex.getMessage());
//					        if (ex.getMessage().contains("410") || ex.getMessage().contains("404"))
//					          subs.delete(s);
					      }
					}catch (Exception e) {
						log.error("unable to send notification");
					}
					
				}
				
				this.facebookLeadConfigService.save(activeConfig);
			}catch(Exception e) {
				e.printStackTrace();
			}
		}
		
		return new ResponseEntity<List<FacebookLeads>>(response,HttpStatus.OK);
	}
	
	public String objectToJson(Object object) {
		 ObjectMapper mapper = new ObjectMapper();  
	      try {
			return mapper.writeValueAsString(object);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
	      return "";
	}

}
