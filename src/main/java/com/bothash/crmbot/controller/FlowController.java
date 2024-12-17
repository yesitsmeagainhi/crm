package com.bothash.crmbot.controller;

import java.io.IOException;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.keycloak.representations.AccessToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.bothash.crmbot.CrmbotApplication;
import com.bothash.crmbot.dto.CloseRequest;
import com.bothash.crmbot.dto.Constants;
import com.bothash.crmbot.dto.CreateTicketRequest;
import com.bothash.crmbot.dto.JustDialCreateRequest;
import com.bothash.crmbot.dto.SchedulerRequest;
import com.bothash.crmbot.dto.TicketFwdRequest;
import com.bothash.crmbot.entity.ActiveTask;
import com.bothash.crmbot.entity.Automation;
import com.bothash.crmbot.entity.AutomationByCourse;
import com.bothash.crmbot.entity.AutomationBySource;
import com.bothash.crmbot.entity.CloseTask;
import com.bothash.crmbot.entity.CounsellingDetails;
import com.bothash.crmbot.entity.FacebookLeads;
import com.bothash.crmbot.entity.HistoryEvents;
import com.bothash.crmbot.service.ActiveTaskService;
import com.bothash.crmbot.service.AutomationByCampaignService;
import com.bothash.crmbot.service.AutomationByCourseService;
import com.bothash.crmbot.service.AutomationBySourceService;
import com.bothash.crmbot.service.AutomationService;
import com.bothash.crmbot.service.CloseTaskService;
import com.bothash.crmbot.service.CounsellingDetailsService;
import com.bothash.crmbot.service.FacebookLeadsService;
import com.bothash.crmbot.service.HistoryEventsService;
import com.bothash.crmbot.service.impl.TaskListener;
import com.bothash.crmbot.service.impl.WhatsappService;
import com.bothash.crmbot.spec.ExcelHelper;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/crmbot/flow")
@Slf4j
public class FlowController {

	@Autowired
	private RestTemplate restTemplate;
	
	@Value("${keycloak.auth-server-url}")
	private String keycloackUrl;
	
	@Autowired
	private ActiveTaskService activeTaskService;
	
	@Autowired
	private HistoryEventsService historyEventsService;
	
	@Autowired
	private CloseTaskService closeTaskService;
	
	@Autowired
	private FacebookLeadsService facebookLeadsService;
	
	@Autowired
	private CounsellingDetailsService conCounsellingDetailsService;
	
	@Autowired
	private AutomationService automationService;
	
	@Autowired
	private AutomationByCourseService automationByCourseService;
	
	@Autowired
	private AutomationBySourceService automationBySourceService;
	
	@Autowired
	private AutomationByCampaignService automationByCampaignService;
	
	@Autowired
	private TaskListener taskListener;
	
	@Autowired
	private WhatsappService whatsappService;
	
	@Value("${facebook.access.token}")
	private String facebookAccessToken;
	
	@PostMapping("/add")
	public ResponseEntity<ActiveTask> addTicket(@RequestBody CreateTicketRequest createTicketRequest,Principal principal){
		KeycloakAuthenticationToken token = (KeycloakAuthenticationToken) principal;
		AccessToken accessToken = token.getAccount().getKeycloakSecurityContext().getToken();
		
		Set<String> roles=token.getAccount().getRoles();
		String userName=accessToken.getPreferredUsername();
		
		String currentUserRole="";
		if(roles.contains("admin")) {
			currentUserRole="admin";
		}else if(roles.contains("manager")) {
			currentUserRole="manager";
		}else if(roles.contains("telecaller")) {
			currentUserRole="telecaller";
		}else if(roles.contains("counsellor")) {
			currentUserRole="counsellor";
		}
		String phoneNumber=createTicketRequest.getPhoneNumber();
		phoneNumber=phoneNumber.replaceAll("\\+91", "");
		phoneNumber=phoneNumber.replaceAll(" ", "");
		List<ActiveTask> existingTask =this.activeTaskService.getTaskByPhoneNumber(phoneNumber);
		existingTask.addAll(this.activeTaskService.getTaskByPhoneNumber("+91"+phoneNumber));
			FacebookLeads facebookLead=createTicketRequest.getFacebookLeads();
			
			FacebookLeads savedFacebookLead=this.facebookLeadsService.save(facebookLead);
			
			ActiveTask activeTask=createTicketRequest.getActiveTask();
			if(activeTask.getLeadPlatform().equals("0")) {
				activeTask.setLeadPlatform("Offline");
			}
			if(createTicketRequest.getAssignToMe()) {
				activeTask.setAssignee(currentUserRole);
				activeTask.setOwner(accessToken.getPreferredUsername());
			}
			
			if(existingTask!=null && existingTask.size()>0){
				activeTask.setIsDuplicate(true);
				for(ActiveTask existing:existingTask) {
					if(existing.getAssignee()!=null && existing.getAssignee()!="") {
						if(existing.getOwner()!=null && existing.getOwner()!="") {
							activeTask.setOwner(existing.getOwner());
							activeTask.setAssignee(existing.getAssignee());
							try {
								activeTask.setStatus(existing.getStatus());
				    			activeTask.setAssignee(existing.getAssignee());
								activeTask.setOwner(existing.getOwner());
								activeTask.setManagerName(existing.getManagerName());
								activeTask.setTelecallerName(existing.getTelecallerName());
								activeTask.setCounsellorName(existing.getCounsellorName());
				    		}catch(Exception e) {
				    			e.printStackTrace();
				    		}
							break;
						}
					}
				}
			}
			
			
			if(activeTask.getAssignee()==null ) {
				List<Automation> automationList=automationService.getByIsActive(true);
				if(automationList.size()>0) {
					for(Automation automationParamter:automationList) {
						Map<String,String> userToAllocate =new HashMap<String,String>();
						if(automationParamter.getParamter().equals("Source")) {
							userToAllocate=this.automationBySourceService.allocate(activeTask.getLeadPlatform());
							
							
						}else if(automationParamter.getParamter().equals("Course")) {
							userToAllocate=this.automationByCourseService.allocate(activeTask.getCourse());
							
						}else if(automationParamter.getParamter().equals("Random")) {
							userToAllocate=this.activeTaskService.randomlyAssgin(activeTask, currentUserRole);
							
						}else if(automationParamter.getParamter().equals("Campaign")) {
							userToAllocate=this.automationByCampaignService.allocate(activeTask.getCampaign());
							
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
								break;
							}
						}catch(Exception e) {
							e.printStackTrace();
						}
					}
					//Automation automationParamter=automationList.get(0);
					
					
				}
			}
			
			if(activeTask.getAssignee()==null) {
				activeTask.setAssignee("admin");
				activeTask.setTaskGroup("admin");
			}else if(activeTask.getAssignee().equals("manager")) {
				activeTask.setManagerName(activeTask.getOwner());
			}else if(activeTask.getAssignee().equals("telecaller")) {
				activeTask.setTelecallerName(activeTask.getOwner());
			}else if(activeTask.getAssignee().equals("counsellor")) {
				activeTask.setCounsellorName(activeTask.getOwner());
			}
			
			activeTask.setPhoneNumber(phoneNumber);	
			activeTask.setFacebookLeads(savedFacebookLead);
			activeTask.setLeadName(createTicketRequest.getLeadName());
			
			if(activeTask.getOwner()!=null && createTicketRequest.getOwnerName()!=null &&  createTicketRequest.getOwnerName().length()>0) {
				activeTask.setStatus("Assgined to "+createTicketRequest.getOwnerName());
			}
			
			activeTask=this.activeTaskService.save(activeTask);
			
			
			HistoryEvents hisEvents=new HistoryEvents();
			hisEvents.setActiveTask(activeTask);
			hisEvents.setUserName(createTicketRequest.getUserName());
			hisEvents.setUserEmail(createTicketRequest.getUserEmail());
			hisEvents.setUserId(createTicketRequest.getUserId());
			hisEvents.setEvent("Task created by "+createTicketRequest.getUserName());
			historyEventsService.save(hisEvents);
			
			if(activeTask.getIsScheduled()!=null && activeTask.getIsScheduled()) {
				HistoryEvents hisEvents2=new HistoryEvents();
				hisEvents2.setActiveTask(activeTask);
				hisEvents2.setUserName(createTicketRequest.getUserName());
				hisEvents2.setUserEmail(createTicketRequest.getUserEmail());
				hisEvents2.setUserId(createTicketRequest.getUserId());
				hisEvents2.setEvent("Task scheduled by "+createTicketRequest.getUserName());
				hisEvents2.setRemark(activeTask.getScheduleComment());
				historyEventsService.save(hisEvents2);
			}
			try {
				
				JSONArray fieldData=new JSONArray(savedFacebookLead.getFieldData().toString());
				if(fieldData.length()>2) {
					JSONObject fieldObject=fieldData.getJSONObject(2);
					if(fieldObject.has("name") && fieldObject.getString("name").equalsIgnoreCase("email")) {
						JSONArray mails=new JSONArray(fieldObject.get("values").toString());
						taskListener.sendTicketCreationMai(activeTask.getCampaign(), mails.get(0).toString());
					}
					
				}
			}catch(Exception e) {
				e.printStackTrace();
			}
			
			try {
				whatsappService.sendMessage(activeTask.getCampaign(),phoneNumber);
			}catch(Exception e2) {
				e2.printStackTrace();
			}
			return new ResponseEntity<ActiveTask>(activeTask,HttpStatus.OK);
		
	}
	
	@PostMapping(value="/upload-excel",consumes = {"multipart/form-data"})
	public ResponseEntity<String> uploadExcel(@RequestPart MultipartFile multipartFile,@RequestPart String userId,@RequestPart String userName,@RequestPart String userEmail,Principal principal){
		KeycloakAuthenticationToken token = (KeycloakAuthenticationToken) principal;
		AccessToken accessToken = token.getAccount().getKeycloakSecurityContext().getToken();
		
		Set<String> roles=token.getAccount().getRoles();
		
		String currentUserRole="";
		if(roles.contains("admin")) {
			currentUserRole="admin";
		}else if(roles.contains("manager")) {
			currentUserRole="manager";
		}else if(roles.contains("telecaller")) {
			currentUserRole="telecaller";
		}else if(roles.contains("counsellor")) {
			currentUserRole="counsellor";
		}
		
		ExcelHelper excelHelper=new ExcelHelper();
		if(excelHelper.hasExcelFormat(multipartFile)) {
			try {
				List<ActiveTask> activeTasks=excelHelper.excelToTasks(multipartFile.getInputStream());
				
				for( ActiveTask activeTask : activeTasks) {
					FacebookLeads faceBookLead=this.facebookLeadsService.save(activeTask.getFacebookLeads());
					activeTask.setFacebookLeads(faceBookLead);
					activeTask.setAssignee("admin");
					activeTask.setIsActive(true);
					activeTask.setIsClaimed(false);
					activeTask.setTaskGroup("admin");
					activeTask.setTaskName("");
					activeTask.setStatus("Open");
					activeTask.setAssignedTime(LocalDateTime.now().plusHours(5).plusMinutes(30));
					
					List<Automation> automationList=automationService.getByIsActive(true);
					if(automationList.size()>0) {
						Automation automationParamter=automationList.get(0);
						Map<String,String> userToAllocate =new HashMap<String,String>();
						if(automationParamter.getParamter().equals("Source")) {
							userToAllocate=this.automationBySourceService.allocate(activeTask.getLeadPlatform());
							
							
						}else if(automationParamter.getParamter().equals("Cousre")) {
							userToAllocate=this.automationByCourseService.allocate(activeTask.getCourse());
							
						}else if(automationParamter.getParamter().equals("Random")) {
							userToAllocate=this.activeTaskService.randomlyAssgin(activeTask, currentUserRole);
							
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
					
					List<ActiveTask> existingTask=this.activeTaskService.getTaskByPhoneNumber(activeTask.getPhoneNumber());
					if(existingTask!=null && existingTask.size()>0){
						activeTask.setIsDuplicate(true);
					}
					this.activeTaskService.save(activeTask);
					
					HistoryEvents hisEvents=new HistoryEvents();
					hisEvents.setActiveTask(activeTask);
					hisEvents.setUserName(userName);
					hisEvents.setUserEmail(userEmail);
					hisEvents.setUserId(userId);
					hisEvents.setEvent("Task created by "+userName+" using excel");
					historyEventsService.save(hisEvents);
				}
				
			} catch (IOException e) {
				e.printStackTrace();
				return new ResponseEntity<String>("failure",HttpStatus.OK);
				
			}
		}
		return new ResponseEntity<String>("success",HttpStatus.OK);
	}
	
	@PutMapping("/update-field-data/{facebookLeadId}/{taskId}")
	public ResponseEntity<FacebookLeads> updateTask(@RequestBody CreateTicketRequest createTicketRequest,@PathVariable Long facebookLeadId,@PathVariable Long taskId){
		
		
		FacebookLeads currFacebookLead=this.facebookLeadsService.getById(facebookLeadId);
		currFacebookLead.setFieldData(createTicketRequest.getFacebookLeads().getFieldData());
		currFacebookLead=this.facebookLeadsService.save(currFacebookLead);
		
		ActiveTask activeTask=this.activeTaskService.getTaskById(taskId);
		activeTask.setState(createTicketRequest.getActiveTask().getState());
		activeTask.setCity(createTicketRequest.getActiveTask().getCity());
		activeTask.setArea(createTicketRequest.getActiveTask().getArea());
		activeTask.setCollege(createTicketRequest.getActiveTask().getCollege());
		activeTask.setCourse(createTicketRequest.getActiveTask().getCourse());
		activeTask.setLeadName(createTicketRequest.getActiveTask().getLeadName());
		activeTask.setTenthPercent(createTicketRequest.getActiveTask().getTenthPercent());
		activeTask.setTwelethPercent(createTicketRequest.getActiveTask().getTwelethPercent());
		activeTask.setNeetPercent(createTicketRequest.getActiveTask().getNeetPercent());
		activeTask.setPhoneNumber2(createTicketRequest.getActiveTask().getPhoneNumber2());
		if(createTicketRequest.getActiveTask().getPhoneNumber() !=null)
			activeTask.setPhoneNumber(createTicketRequest.getActiveTask().getPhoneNumber());
		activeTask.setLeadType(createTicketRequest.getActiveTask().getLeadType());
		this.activeTaskService.save(activeTask);
		
		HistoryEvents hisEvents=new HistoryEvents();
		hisEvents.setActiveTask(activeTask);
		hisEvents.setUserName(createTicketRequest.getUserName());
		hisEvents.setUserEmail(createTicketRequest.getUserEmail());
		hisEvents.setUserId(createTicketRequest.getUserId());
		hisEvents.setEvent("Field data updated by "+createTicketRequest.getUserName());
		historyEventsService.save(hisEvents);
		
		return new ResponseEntity<FacebookLeads>(currFacebookLead,HttpStatus.OK);
	}
	
	@PutMapping("/fwd")
	public ResponseEntity<ActiveTask> fwdTicket(@RequestBody TicketFwdRequest ticketFwdRequest){
		
		ActiveTask activeTask=activeTaskService.getTaskById(ticketFwdRequest.getTaskId());
		activeTask.setIsClaimed(false);
		activeTask.setClaimTime(null);
		activeTask.setAssignedTime(LocalDateTime.now().plusHours(5).plusMinutes(30));
		activeTask.setOwner(ticketFwdRequest.getUserEmail());
		activeTask.setAssignee(ticketFwdRequest.getUserGroup());
		activeTask.setTaskGroup("");
		activeTask.setTaskName("");
		activeTask.setRemark(ticketFwdRequest.getRemark());
		activeTask.setStatus("Assgined to "+ticketFwdRequest.getUserName());
		
		if(ticketFwdRequest.getUserGroup().equalsIgnoreCase(Constants.manager)) {
			activeTask.setManagerName(ticketFwdRequest.getUserEmail());
		}else if(ticketFwdRequest.getUserGroup().equalsIgnoreCase(Constants.telecaller)) {
			activeTask.setTelecallerName(ticketFwdRequest.getUserEmail());
		}else if(ticketFwdRequest.getUserGroup().equalsIgnoreCase(Constants.counsellor)) {
			activeTask.setCounsellorName(ticketFwdRequest.getUserEmail());
		}
		
		activeTaskService.save(activeTask);
		
		HistoryEvents hisEvents=new HistoryEvents();
		hisEvents.setActiveTask(activeTask);
		hisEvents.setUserName(ticketFwdRequest.getForwarderUserName());
		hisEvents.setUserEmail(ticketFwdRequest.getForwarderUserEmail());
		hisEvents.setUserId(ticketFwdRequest.getForwarderUserId());
		hisEvents.setEvent("Task assigned to "+ticketFwdRequest.getUserName());
		hisEvents.setRemark(ticketFwdRequest.getRemark());
		historyEventsService.save(hisEvents);
		return new ResponseEntity<ActiveTask>(activeTask,HttpStatus.OK);
	}
	
	@PutMapping("/schedule")
	public ResponseEntity<ActiveTask> schedule(@RequestBody SchedulerRequest schedulerRequest){
		log.info(schedulerRequest.getScheduleTime()+"");
		ActiveTask activeTask=this.activeTaskService.getTaskById(schedulerRequest.getTaskId());
		activeTask.setIsScheduled(true);
		activeTask.setScheduleTime(schedulerRequest.getScheduleTime());
		activeTask.setScheduleComment(schedulerRequest.getComment());
		activeTask.setSchedulerName(schedulerRequest.getSchedulerName());
		activeTask.setSchedulerEmail(schedulerRequest.getSchedulerEmail());
		activeTask.setSchedulerUserId(schedulerRequest.getSchedulerUserId());
		this.activeTaskService.save(activeTask);
		
		HistoryEvents hisEvents=new HistoryEvents();
		hisEvents.setActiveTask(activeTask);
		hisEvents.setUserName(schedulerRequest.getSchedulerName());
		hisEvents.setUserEmail(schedulerRequest.getSchedulerEmail());
		hisEvents.setUserId(schedulerRequest.getSchedulerUserId());
		hisEvents.setEvent("Task scheduled by "+schedulerRequest.getSchedulerName());
		hisEvents.setRemark(schedulerRequest.getComment());
		historyEventsService.save(hisEvents);
		return new ResponseEntity<ActiveTask>(activeTask,HttpStatus.OK);
	}
	
	@PutMapping("/complete-schedule")
	public ResponseEntity<ActiveTask> completeSchedule(@RequestBody SchedulerRequest schedulerRequest){
		log.info(schedulerRequest.getScheduleTime()+"");
		ActiveTask activeTask=this.activeTaskService.getTaskById(schedulerRequest.getTaskId());
		activeTask.setIsScheduled(false);
		activeTask.setScheduleTime(null);
		activeTask.setScheduleComment("");
		activeTask.setSchedulerName("");
		activeTask.setSchedulerEmail("");
		activeTask.setSchedulerUserId("");
		this.activeTaskService.save(activeTask);
		
		HistoryEvents hisEvents=new HistoryEvents();
		hisEvents.setActiveTask(activeTask);
		hisEvents.setUserName(schedulerRequest.getSchedulerName());
		hisEvents.setUserEmail(schedulerRequest.getSchedulerEmail());
		hisEvents.setUserId(schedulerRequest.getSchedulerUserId());
		hisEvents.setEvent("Task scheduled completed by "+schedulerRequest.getSchedulerName());
		historyEventsService.save(hisEvents);
		return new ResponseEntity<ActiveTask>(activeTask,HttpStatus.OK);
	}
	
	@PutMapping("/close-task")
	public ResponseEntity<String> closeTask(@RequestBody CloseRequest closeRequest){
		ActiveTask activeTask=this.activeTaskService.getTaskById(closeRequest.getTaskId());
		
		//if(closeRequest.getIsTaskCompleted()!=null && closeRequest.getIsTaskCompleted()) {
		//	activeTask.setIsCompleted(true);
		//}
		activeTask.setClosingRemark(closeRequest.getRemark());
		activeTask.setIsConverted(closeRequest.getIsConverted());
		activeTask.setIsSeatConfirmed(closeRequest.getIsSeatConfirmed());
		activeTask.setIsActive(true);
		this.activeTaskService.save(activeTask);
		HistoryEvents hisEvents=new HistoryEvents();
		hisEvents.setActiveTask(activeTask);
		hisEvents.setUserName(closeRequest.getUserName());
		hisEvents.setUserEmail(closeRequest.getUserEmail());
		hisEvents.setUserId(closeRequest.getUserId());
		hisEvents.setRemark(closeRequest.getRemark());
		
		if(!closeRequest.getCloseTask()) {
			CloseTask closeTask=this.closeTaskService.getByActiveTask(closeRequest.getTaskId());
			if(closeTask!=null)
				closeTaskService.delete(closeTask);
		}
		
		if(closeRequest.getCloseTask()) {
			activeTask.setIsActive(false);
			activeTask.setStatus(Constants.closed);
			this.activeTaskService.save(activeTask);
			
			CloseTask closeTask=this.closeTaskService.getByActiveTask(closeRequest.getTaskId());
			if(closeTask==null) {
				closeTask=new CloseTask();
			}
			closeTask.setActiveTask(activeTask);
			closeTask.setIsConverted(closeRequest.getIsConverted());
			closeTask.setRemark(closeRequest.getRemark());
			if(closeTask.getIsConverted())
				closeTask.setClosingId("CONV_LD_");
			this.closeTaskService.save(closeTask);
			
			hisEvents.setEvent("Task closed by "+closeRequest.getUserName());
			
			historyEventsService.save(hisEvents);
			if(closeRequest.getIsConverted())
				return new ResponseEntity<String>("Ticket closed with id : CONV_ID_"+closeTask.getId(),HttpStatus.OK);
			else {
				return new ResponseEntity<String>("Ticket closed",HttpStatus.OK);
			}
		}else {
			
			hisEvents.setEvent("Closing details saved by by "+closeRequest.getUserName());
			historyEventsService.save(hisEvents);
			return new ResponseEntity<String>("Successfully saved",HttpStatus.OK);
		}		
	}
	
	@PutMapping("/complete-task")
	public ResponseEntity<String> completeTask(@RequestBody CloseRequest closeRequest){
		ActiveTask activeTask=this.activeTaskService.getTaskById(closeRequest.getTaskId());
		
		//if(closeRequest.getIsTaskCompleted()!=null && closeRequest.getIsTaskCompleted()) {
		//	activeTask.setIsCompleted(true);
		//}
		activeTask.setClosingRemark(closeRequest.getRemark());
		HistoryEvents hisEvents=new HistoryEvents();
		hisEvents.setActiveTask(activeTask);
		hisEvents.setUserName(closeRequest.getUserName());
		hisEvents.setUserEmail(closeRequest.getUserEmail());
		hisEvents.setUserId(closeRequest.getUserId());
		hisEvents.setRemark(closeRequest.getRemark());
		activeTask.setIsActive(true);
		activeTask.setStatus("Completed");
		activeTask.setAssignee("manager");
		activeTask.setOwner(activeTask.getManagerName());
		this.activeTaskService.save(activeTask);
		
		
		
		hisEvents.setEvent("Task completed by "+closeRequest.getUserName());
		
		historyEventsService.save(hisEvents);
		
		return new ResponseEntity<String>("Ticket Completed",HttpStatus.OK);
		
		
	}
	
	@PostMapping("/counselling/save")
	public ResponseEntity<CounsellingDetails> saveCounsellingDetails(@RequestBody CounsellingDetails counsellingDetails,@RequestParam Long activeTaskId){
		ActiveTask activeTask=this.activeTaskService.getTaskById(activeTaskId);
		if(counsellingDetails.getIsCounselled()) {
			activeTask.setIsCounsellingDone(true);
		}else if(activeTask.getIsCounsellingDone()==null) {
			activeTask.setIsCounsellingDone(false);
		}
		this.activeTaskService.save(activeTask);
		counsellingDetails.setActiveTask(activeTask);
		CounsellingDetails saveCounsellingDetails=this.conCounsellingDetailsService.save(counsellingDetails);
		return new ResponseEntity<CounsellingDetails>(saveCounsellingDetails,HttpStatus.OK);
	}
	
	
	
	@PostMapping("/justdial-create")
	public  ResponseEntity<ActiveTask> justDialApi(@RequestBody JustDialCreateRequest justDialCreateRequest,Principal principal){
		KeycloakAuthenticationToken token = (KeycloakAuthenticationToken) principal;
		AccessToken accessToken = token.getAccount().getKeycloakSecurityContext().getToken();
		
		Set<String> roles=token.getAccount().getRoles();
		String userName=accessToken.getPreferredUsername();
		
		String currentUserRole="";
		if(roles.contains("admin")) {
			currentUserRole="admin";
		}else if(roles.contains("manager")) {
			currentUserRole="manager";
		}else if(roles.contains("telecaller")) {
			currentUserRole="telecaller";
		}else if(roles.contains("counsellor")) {
			currentUserRole="counsellor";
		}
		String phoneNumber=justDialCreateRequest.getMobile();
		phoneNumber=phoneNumber.replaceAll("\\+91", "");
		phoneNumber=phoneNumber.replaceAll(" ", "");
		List<ActiveTask> existingTask =this.activeTaskService.getTaskByPhoneNumber(phoneNumber);
		
		FacebookLeads facebookLead=new FacebookLeads();
		 JSONArray fieldValues=new JSONArray();
		 try {
			 JSONObject leadName=new JSONObject();
				leadName.put("name", "full_name");
				String values[]=new  String[1];
				values[0]=justDialCreateRequest.getName();
				leadName.put("values", values);
				fieldValues.put(leadName);
		 }catch(Exception e) {
			 e.printStackTrace();
		 }
		 try {
		    JSONObject phoneNumberJson=new JSONObject();
		    phoneNumberJson.put("name", "phone_number");
			String values[]=new  String[1];
			values[0]=phoneNumber;
			phoneNumberJson.put("values", values);
			fieldValues.put(phoneNumberJson);
		 }catch(Exception e) {
			 e.printStackTrace();
		 }
		 try {
			    JSONObject email=new JSONObject();
			    email.put("name", "email");
				String values[]=new  String[1];
				values[0]=justDialCreateRequest.getEmail();
				email.put("values", values);
				fieldValues.put(email);
			 }catch(Exception e) {
				 e.printStackTrace();
			 }
		
		
		facebookLead.setFieldData(fieldValues.toString());
		FacebookLeads savedFacebookLead=this.facebookLeadsService.save(facebookLead);
		
		ActiveTask activeTask=new ActiveTask();
		
		activeTask.setCampaign(justDialCreateRequest.getCompany());
		activeTask.setArea(justDialCreateRequest.getArea());
		activeTask.setCity(justDialCreateRequest.getCity());
		//activeTask.setState(justDialCreateRequest.gets());
		activeTask.setRequirement(justDialCreateRequest.getCategory());
		activeTask.setLeadPlatform("J");
		if(existingTask!=null && existingTask.size()>0){
			activeTask.setIsDuplicate(true);
			for(ActiveTask existing:existingTask) {
				if(existing.getAssignee()!=null && existing.getAssignee()!="") {
					if(existing.getOwner()!=null && existing.getOwner()!="") {
						activeTask.setOwner(existing.getOwner());
						activeTask.setAssignee(existing.getAssignee());
						try {
			    			activeTask.setAssignee(existing.getAssignee());
							activeTask.setOwner(existing.getOwner());
							activeTask.setManagerName(existing.getManagerName());
							activeTask.setTelecallerName(existing.getTelecallerName());
							activeTask.setCounsellorName(existing.getCounsellorName());
			    		}catch(Exception e) {
			    			e.printStackTrace();
			    		}
						break;
					}
				}
			}
		}
		
		
		if(activeTask.getAssignee()==null ) {
			List<Automation> automationList=automationService.getByIsActive(true);
			if(automationList.size()>0) {
				for(Automation automationParamter:automationList) {
					Map<String,String> userToAllocate =new HashMap<String,String>();
					if(automationParamter.getParamter().equals("Source")) {
						userToAllocate=this.automationBySourceService.allocate(activeTask.getLeadPlatform());
						
						
					}else if(automationParamter.getParamter().equals("Course") && activeTask.getCourse()!=null) {
						userToAllocate=this.automationByCourseService.allocate(activeTask.getCourse());
						
					}else if(automationParamter.getParamter().equals("Random")) {
						userToAllocate=this.activeTaskService.randomlyAssgin(activeTask, currentUserRole);
						
					}else if(automationParamter.getParamter().equals("Campaign")) {
						userToAllocate=this.automationByCampaignService.allocate(activeTask.getCampaign());
						
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
							break;
						}
					}catch(Exception e) {
						e.printStackTrace();
					}
				}
				//Automation automationParamter=automationList.get(0);
				
				
			}
		}
			
		if(activeTask.getAssignee()==null) {
			activeTask.setAssignee("admin");
			activeTask.setTaskGroup("admin");
		}else if(activeTask.getAssignee().equals("manager")) {
			activeTask.setManagerName(activeTask.getOwner());
		}else if(activeTask.getAssignee().equals("telecaller")) {
			activeTask.setTelecallerName(activeTask.getOwner());
		}else if(activeTask.getAssignee().equals("counsellor")) {
			activeTask.setCounsellorName(activeTask.getOwner());
		}
		
		activeTask.setPhoneNumber(phoneNumber);	
		activeTask.setFacebookLeads(savedFacebookLead);
		activeTask.setLeadName(justDialCreateRequest.getName());
		
		
		activeTask.setIsActive(true);
		activeTask.setIsClaimed(false);
		activeTask.setTaskGroup("admin");
		activeTask.setTaskName("");
		activeTask.setStatus("Open");
		activeTask.setAssignedTime(LocalDateTime.now().plusHours(5).plusMinutes(30));
		
		//if(activeTask.getOwner()!=null && justDialCreateRequest.getOwnerName()!=null &&  justDialCreateRequest.getOwnerName().length()>0) {
		//	activeTask.setStatus("Assgined to "+justDialCreateRequest.getOwnerName());
		//}
		
		activeTask=this.activeTaskService.save(activeTask);
		
		
		HistoryEvents hisEvents=new HistoryEvents();
		hisEvents.setUserName("Email");
		hisEvents.setUserEmail("Email");
		hisEvents.setUserId("Email");
		hisEvents.setEvent("Task created through email");
		historyEventsService.save(hisEvents);
		
		
		try {
			
			JSONArray fieldData=new JSONArray(savedFacebookLead.getFieldData().toString());
			if(fieldData.length()>2) {
				JSONObject fieldObject=fieldData.getJSONObject(2);
				if(fieldObject.has("name") && fieldObject.getString("name").equalsIgnoreCase("email")) {
					JSONArray mails=new JSONArray(fieldObject.get("values").toString());
					taskListener.sendTicketCreationMai(activeTask.getCampaign(), mails.get(0).toString());
				}
				
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		try {
			whatsappService.sendMessage(activeTask.getCampaign(),phoneNumber);
		}catch(Exception e2) {
			e2.printStackTrace();
		}
		return new ResponseEntity<ActiveTask>(activeTask,HttpStatus.OK);
		
	}
	
	@PutMapping("/transfer-leads")
	public  ResponseEntity<String> transferLeads(@RequestParam String fromUserName,@RequestParam String toUserName,Principal principal){
		int response = this.activeTaskService.transferLeads(toUserName, fromUserName);
		return new ResponseEntity<String>("success",HttpStatus.OK);
	}
	
	@GetMapping("/total-leads")
	public  ResponseEntity<Integer> getTotalLeadOfUser(@RequestParam String userName,@RequestParam String role,@RequestParam String course,@RequestParam String platform,Principal principal){
		List<ActiveTask> response = this.activeTaskService.getByOwnerAndActiveAndCourseAndPlatform(role, userName,course,platform);
		return new ResponseEntity<Integer>(response.size(),HttpStatus.OK);
	}
}
