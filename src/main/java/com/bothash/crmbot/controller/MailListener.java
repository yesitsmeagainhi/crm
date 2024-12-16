package com.bothash.crmbot.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.Address;
import javax.mail.FetchProfile;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.NoSuchProviderException;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.MimeBodyPart;
import javax.mail.search.FlagTerm;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;

import com.bothash.crmbot.entity.ActiveTask;
import com.bothash.crmbot.entity.Automation;
import com.bothash.crmbot.entity.FacebookLeadConfigs;
import com.bothash.crmbot.entity.FacebookLeads;
import com.bothash.crmbot.entity.HistoryEvents;
import com.bothash.crmbot.entity.TargetMails;
import com.bothash.crmbot.service.ActiveTaskService;
import com.bothash.crmbot.service.AutomationByCampaignService;
import com.bothash.crmbot.service.AutomationByCourseService;
import com.bothash.crmbot.service.AutomationBySourceService;
import com.bothash.crmbot.service.AutomationService;
import com.bothash.crmbot.service.FacebookLeadConfigService;
import com.bothash.crmbot.service.FacebookLeadsService;
import com.bothash.crmbot.service.HistoryEventsService;
import com.bothash.crmbot.service.TargetMailsService;
import com.bothash.crmbot.service.impl.TaskListener;
import com.bothash.crmbot.service.impl.WhatsappService;
import com.fasterxml.jackson.databind.ObjectMapper;

@Controller
public class MailListener {
	 private static final Logger log = LoggerFactory.getLogger(MailListener.class);
	@Autowired
	private FacebookLeadsService facebookLeadsService;
	
	@Autowired
	private ActiveTaskService activeTaskService;
	
	@Autowired
	private FacebookLeadConfigService facebookLeadConfigService;
	
	@Autowired
	private HistoryEventsService historyEventsService;
	
	@Autowired
	private AutomationService automationService;
	
	@Autowired
	private AutomationByCourseService automationByCourseService;
	
	@Autowired
	private AutomationBySourceService automationBySourceService;
	
	@Autowired
	private AutomationByCampaignService automationByCampaignService;
	
	@Autowired
	private TargetMailsService targetMailsService;
	
	@Autowired
	private TaskListener taskListener;
	
	@Autowired
	private WhatsappService whatsappService;
	
	@Value("${email.user.name}")
	private String userName;
	
	@Value("${email.password}")
	private String password;
	
	private Properties getServerProperties() {
		Properties properties = new Properties();
		properties.put("mail.pop3.host", "pop.gmail.com");
      properties.put("mail.pop3.port", "995");
      properties.put("mail.pop3.starttls.enable", "true");

	      return properties;
	   }

	@Scheduled(fixedRate = 110000)
	   public void getNewEmails() {
		
		try  
		{
			
			List<TargetMails> targetMails=targetMailsService.getAllByIsActive(true);
			
			for(TargetMails targetMail:targetMails) {
				System.out.println("Checking mails NEW.");
				Properties props = new Properties();
			    props.put("mail.store.protocol","imaps");
			    Session session = Session.getDefaultInstance(props, null);
			    Store store = session.getStore("imaps");
		        store.connect("imap.googlemail.com",targetMail.getMailId(), targetMail.getAppPassword());

		        Folder inbox = store.getFolder("inbox");

		        inbox.open(Folder.READ_WRITE);

			    

			      // retrieve the messages from the folder in an array and print it
			      Message[] messages = inbox.search(new FlagTerm(new Flags(
		                  Flags.Flag.SEEN), false));
			      
			      FetchProfile fp = new FetchProfile();
		          fp.add(FetchProfile.Item.ENVELOPE);

		          fp.add(FetchProfile.Item.CONTENT_INFO);

		          //emailFolder.fetch(messages, fp);
			      // Message[] messages = fetchMessages("pop.gmail.com", "stormbreaker8855@gmail.com", "ocemczedoljhdmwn",true);
			      System.out.println("messages.length---" + messages.length);

			      for (int i = 0, n = messages.length; i < n; i++) {
			         Message message = messages[i];
			        
			         
		        	 System.out.println("Mail from just dial");
		        	 System.out.println("---------------------------------");
			         System.out.println("Email Number " + (i + 1));
			         System.out.println("Subject: " + message.getHeader("subject"));
			         System.out.println("From: " + message.getFrom()[0]);
			         System.out.println("FOrm name "+message.getFrom());
			         System.out.println("Text: " + message.getContent().toString());
			         System.out.println("Time: " + message.getSentDate());
		        	 writePart(message,message.getSubject(),message.getFrom()[0].toString());
			         
			         message.setFlag(Flags.Flag.SEEN, true);
			         
			      }

			      //close the store and folder objects
			      inbox.close(false);
			      store.close();
			}

	      } catch (NoSuchProviderException e) {
	         e.printStackTrace();
	      } catch (MessagingException e) {
	         e.printStackTrace();
	      } catch (Exception e) {
	         e.printStackTrace();
	      }
	}
	
	
	
	public  void writePart(Part p,String subject,String from) throws Exception {
	      System.out.println("----------------------------");
	      System.out.println("CONTENT-TYPE: " + p.getContentType());
	      List<FacebookLeadConfigs> allCampaings=facebookLeadConfigService.getAllActiveCongifs("Google");
	      
	      ArrayList<String> googleCampaings=new ArrayList<>();
	      
	      for(FacebookLeadConfigs campaign:allCampaings) {
	    	  googleCampaings.add(campaign.getCampaignName());
	      }
	      String mainSubject=from.split("<")[0];
	      mainSubject=mainSubject.trim();
	      //check if the content is plain text
	      if (p.isMimeType("text/plain")) {
	         System.out.println("This is plain text");
	         System.out.println("---------------------------");
	         System.out.println((String) p.getContent());
	         if(from.contains("Just Dial")) {
	        	 log.info("JUST DIAL MAIL READ");
	        	 //generateJustDialLead((String) p.getContent(), subject);
	         }
	        	 
	         else if(googleCampaings.contains(mainSubject)) {
	        	 System.out.println("This is " +mainSubject);
	         }else if(subject.contains("Google Ads Lead")) {
	        	 generateCustomeGoogleLead((String) p.getContent(), subject);
	         }
	      } else if (p.isMimeType("TEXT/HTML") ) {
		         System.out.println("This is plain text");
		         System.out.println("---------------------------");
		         System.out.println((String) p.getContent());
		         if(from.contains("Just Dial"))
		        	 System.out.println(p.getContent().toString());
		        	 //generateJustDialLead((String) p.getContent(), subject);
		         else if(googleCampaings.contains(mainSubject)) {
		        	 generateGoogleLead((String) p.getContent(), subject);
		         }else if(subject.contains("Google Ads Lead") || subject.contains("PWD")) {
		        	 generateCustomeGoogleLead((String) p.getContent(), subject);
		         }
		      } 
	      //check if the content has attachment
	      else if (p.isMimeType("multipart/*")) {
		         System.out.println("This is a Multipart");
		         System.out.println("---------------------------");
		         Multipart mp = (Multipart) p.getContent();
		         int count = mp.getCount();
		         for (int i = 0; i < count; i++)
		            writePart(mp.getBodyPart(i),subject,from);
		      } 
		      //check if the content is a nested message
		      else if (p.isMimeType("message/rfc822")) {
		         System.out.println("This is a Nested Message");
		         System.out.println("---------------------------");
		         writePart((Part) p.getContent(),subject,from);
		      } 
		      

	   }
	
	private void generateCustomeGoogleLead(String content, String subject) {
		String regex= "(Full[^\\w]*Name|Name|First[^\\w]*Name)((?:\\S*\\s*\\n?){1,12})";
		Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(content);
        ActiveTask activeTask=new ActiveTask();
        String plafrom="G";
        
        //subject=subject.replace("Enquiry for ", "");
        try{
        	String[] campaings=subject.split("-");
    		if(campaings.length>1) {
    			subject=campaings[1];
    		}
    		if(campaings.length>2) {
    			plafrom=campaings[2];
    		}
    		activeTask.setCampaign(subject);
        }catch(Exception e2) {
        	e2.printStackTrace();
        }
		
		
        JSONArray fieldValues=new JSONArray();
        while (matcher.find()) {
        	try {
		    	String fullName=matcher.group(2);
		    	fullName=fullName.split("\n")[0];
		    	fullName=fullName.split("<br />")[1];
		    	fullName=fullName.split("</li>")[0];
		    	System.out.println("fullName   "+ fullName);
		    	activeTask.setLeadName(fullName.trim());
		    	
		    	JSONObject leadName=new JSONObject();
				leadName.put("name", "full_name");
				String values[]=new  String[1];
				values[0]=fullName;
				leadName.put("values", values);
				fieldValues.put(leadName);
        	}catch(Exception e) {
        		e.printStackTrace();
        	}
        }
        regex= "(Contact[^\\w]*Number|Phone[^\\w]*number|Number|Numbers|Phone)((?:\\S*\\s*\\n?){1,10})";
		pattern = Pattern.compile(regex, Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);
        matcher = pattern.matcher(content);
        while (matcher.find()) {
        	try {
		    	String phoneNumber=matcher.group(2);
		    	System.out.println("fullName   "+ phoneNumber);
		    	phoneNumber=phoneNumber.split("\n")[0];
		    	phoneNumber=phoneNumber.split("<br />")[1];
		    	phoneNumber=phoneNumber.split("</li>")[0];
		    	phoneNumber=phoneNumber.trim();
		    	phoneNumber=phoneNumber.replaceAll(" ", "");
		    	phoneNumber=phoneNumber.replace("\\+91", "");
		    	System.out.println("phoneNumber   "+ phoneNumber);
		    	//activeTask.setLeadName(phoneNumber.trim());
		    	activeTask.setPhoneNumber(phoneNumber);
		    	List<ActiveTask> existingTask =this.activeTaskService.getTaskByPhoneNumber(phoneNumber);
		    	try {
		    		existingTask.addAll(this.activeTaskService.getTaskByPhoneNumber("+91"+phoneNumber));
		    	}catch (Exception e) {
		    		e.printStackTrace();
				}
		    	if(existingTask!=null && existingTask.size()>0) {
		    		activeTask.setIsDuplicate(true);
		    		for(ActiveTask existing:existingTask) {
						if(existing.getAssignee()!=null && existing.getAssignee()!="") {
							if(existing.getOwner()!=null && existing.getOwner()!="") {
								activeTask.setOwner(existing.getOwner());
								activeTask.setAssignee(existing.getAssignee());
								try {
					    			activeTask.setAssignee(existing.getAssignee());
									activeTask.setOwner(existing.getOwner());
									activeTask.setStatus(existingTask.get(0).getStatus());
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
		    	JSONObject phoneNumberJson=new JSONObject();
		    	phoneNumberJson.put("name", "phone_number");
				String values[]=new  String[1];
				values[0]= phoneNumber;
				
				
				phoneNumberJson.put("values", values);
				fieldValues.put(phoneNumberJson);
        	}catch(Exception e) {
        		e.printStackTrace();
        	}
        }
        
        regex= "(Checkbox)((?:\\S*\\s*\\n?){1,10})";
		pattern = Pattern.compile(regex, Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);
        matcher = pattern.matcher(content);
        while (matcher.find()) {
        	try {
		    	String email=matcher.group(2);
		    	System.out.println("email   "+ email);
		    	email=email.split("\n")[0];
		    	email=email.split("<br />")[1];
		    	email=email.split("</li>")[0];
		    	System.out.println("phoneNumber   "+ email);
		    	//activeTask.setLeadName(email.trim());
		    	activeTask.setCourse(email);
		    	JSONObject emailJson=new JSONObject();
		    	emailJson.put("name", "requirement");
				String values[]=new  String[1];
				values[0]= email;
				emailJson.put("values", values);
				fieldValues.put(emailJson);
				try {
					taskListener.sendTicketCreationMai(activeTask.getCampaign(), email);
				}catch(Exception e3) {
					e3.printStackTrace();
				}
				
        	}catch(Exception e) {
        		e.printStackTrace();
        	}
        }
        
        FacebookLeads facebookLeads=new FacebookLeads();
		facebookLeads.setFieldData(fieldValues.toString());
		facebookLeads=this.facebookLeadsService.save(facebookLeads);
		activeTask.setFacebookLeads(facebookLeads);
		//activeTask.setAssignee("admin");
		activeTask.setLeadPlatform(plafrom);
		activeTask.setIsActive(true);
		activeTask.setIsClaimed(false);
		activeTask.setTaskGroup("admin");
		activeTask.setTaskName("");
		activeTask.setAssignedTime(LocalDateTime.now().plusHours(5).plusMinutes(30));
		if(activeTask.getAssignee()==null) {
			List<Automation> automationList=automationService.getByIsActive(true);
			if(automationList.size()>0) {
				for(Automation automationParamter:automationList) {
					Map<String,String> userToAllocate =new HashMap<String,String>();
					if(automationParamter.getParamter().equals("Source")) {
						userToAllocate=this.automationBySourceService.allocate(activeTask.getLeadPlatform());
						
					}else if(automationParamter.getParamter().equals("Cousre")) {
						userToAllocate=this.automationByCourseService.allocate(activeTask.getCourse());
						
					}else if(automationParamter.getParamter().equals("Random")) {
						userToAllocate=this.activeTaskService.randomlyAssgin(activeTask, "admin");
					
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
				
			}else {
				activeTask.setAssignee("admin");
			}
		}
		
		try {
			whatsappService.sendMessage(activeTask.getCampaign(),activeTask.getPhoneNumber());
		}catch(Exception e2) {
			e2.printStackTrace();
		}
		this.activeTaskService.save(activeTask);
		
		HistoryEvents hisEvents=new HistoryEvents();
		hisEvents.setActiveTask(activeTask);
		hisEvents.setUserName("Email");
		hisEvents.setUserEmail("Email");
		hisEvents.setUserId("Email");
		hisEvents.setEvent("Task created through email");
		historyEventsService.save(hisEvents);
	}

	private void generateGoogleLead(String content, String subject) {
		String regex= "(Full[^\\w]*Name|Name|First[^\\w]*Name)((?:\\S*\\s*\\n?){1,12})";
		Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(content);
        ActiveTask activeTask=new ActiveTask();
        JSONArray fieldValues=new JSONArray();
        while (matcher.find()) {
        	try {
		    	String fullName=matcher.group(2);
		    	fullName=fullName.split("<td")[1];
		    	fullName=fullName.split(">")[1];
		    	fullName=fullName.split("</td")[0];
		    	System.out.println("fullName   "+ fullName);
		    	activeTask.setLeadName(fullName.trim());
		    	
		    	JSONObject leadName=new JSONObject();
				leadName.put("name", "full_name");
				String values[]=new  String[1];
				values[0]=fullName;
				leadName.put("values", values);
				fieldValues.put(leadName);
        	}catch(Exception e) {
        		e.printStackTrace();
        	}
        }
        regex= "(Contact[^\\w]*Number|Phone[^\\w]*number|Number|Numbers)((?:\\S*\\s*\\n?){1,10})";
		pattern = Pattern.compile(regex, Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);
        matcher = pattern.matcher(content);
        while (matcher.find()) {
        	try {
		    	String phoneNumber=matcher.group(2);
		    	System.out.println("fullName   "+ phoneNumber);
		    	phoneNumber=phoneNumber.split("<td")[1];
		    	phoneNumber=phoneNumber.split(">")[1];
		    	phoneNumber=phoneNumber.split("</td")[0];
		    	System.out.println("phoneNumber   "+ phoneNumber);
		    	//activeTask.setLeadName(phoneNumber.trim());
		    	phoneNumber=phoneNumber.replaceAll(" ", "");
		    	phoneNumber=phoneNumber.replace("\\+91", "");
		    	activeTask.setPhoneNumber(phoneNumber);
		    	List<ActiveTask> existingTask =this.activeTaskService.getTaskByPhoneNumber(phoneNumber);
		    	try {
		    		existingTask.addAll(this.activeTaskService.getTaskByPhoneNumber("+91"+phoneNumber));
		    	}catch (Exception e) {
		    		e.printStackTrace();
				}
		    	
		    	if(existingTask!=null && existingTask.size()>0) {
		    		activeTask.setIsDuplicate(true);
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
		    	JSONObject phoneNumberJson=new JSONObject();
		    	phoneNumberJson.put("name", "phone_number");
				String values[]=new  String[1];
				values[0]= phoneNumber;
				
				
				phoneNumberJson.put("values", values);
				fieldValues.put(phoneNumberJson);
        	}catch(Exception e) {
        		e.printStackTrace();
        	}
        }
        
        regex= "(Email[^\\w]*Address|Email)((?:\\S*\\s*\\n?){1,10})";
		pattern = Pattern.compile(regex, Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);
        matcher = pattern.matcher(content);
        while (matcher.find()) {
        	try {
		    	String email=matcher.group(2);
		    	System.out.println("email   "+ email);
		    	email=email.split("<td")[1];
		    	email=email.split(">")[1];
		    	email=email.split("</td")[0];
		    	System.out.println("phoneNumber   "+ email);
		    	//activeTask.setLeadName(email.trim());
		    	
		    	JSONObject emailJson=new JSONObject();
		    	emailJson.put("name", "email");
				String values[]=new  String[1];
				values[0]= email;
				try {
					taskListener.sendTicketCreationMai(activeTask.getCampaign(), email);
				}catch(Exception e3) {
					e3.printStackTrace();
				}
				emailJson.put("values", values);
				fieldValues.put(emailJson);
        	}catch(Exception e) {
        		e.printStackTrace();
        	}
        }
        
        FacebookLeads facebookLeads=new FacebookLeads();
		facebookLeads.setFieldData(fieldValues.toString());
		facebookLeads=this.facebookLeadsService.save(facebookLeads);
		activeTask.setFacebookLeads(facebookLeads);
		//activeTask.setAssignee("admin");
		activeTask.setLeadPlatform("G");
		activeTask.setIsActive(true);
		activeTask.setIsClaimed(false);
		activeTask.setTaskGroup("admin");
		activeTask.setTaskName("");
		activeTask.setAssignedTime(LocalDateTime.now().plusHours(5).plusMinutes(30));
		if(activeTask.getAssignee()==null ) {
			List<Automation> automationList=automationService.getByIsActive(true);
			if(automationList.size()>0) {
				for(Automation automationParamter:automationList) {
					Map<String,String> userToAllocate =new HashMap<String,String>();
					if(automationParamter.getParamter().equals("Source")) {
						userToAllocate=this.automationBySourceService.allocate(activeTask.getLeadPlatform());
						
					}else if(automationParamter.getParamter().equals("Cousre")) {
						userToAllocate=this.automationByCourseService.allocate(activeTask.getCourse());
						
					}else if(automationParamter.getParamter().equals("Random")) {
						userToAllocate=this.activeTaskService.randomlyAssgin(activeTask, "admin");
					
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
				
			}else {
				activeTask.setAssignee("admin");
			}
		}
		
		try {
			whatsappService.sendMessage(activeTask.getCampaign(),activeTask.getPhoneNumber());
		}catch(Exception e2) {
			e2.printStackTrace();
		}
		
		this.activeTaskService.save(activeTask);
		
		HistoryEvents hisEvents=new HistoryEvents();
		hisEvents.setActiveTask(activeTask);
		hisEvents.setUserName("Email");
		hisEvents.setUserEmail("Email");
		hisEvents.setUserId("Email");
		hisEvents.setEvent("Task created through email");
		historyEventsService.save(hisEvents);
		
	}

	public  void generateJustDialLead(String content,String subject) {
		String keys[]=new String[] {"full_name","phone","email","state","city","area","requirement","Search Date & Time",};
		ActiveTask activeTask=new ActiveTask();
		JSONArray fieldValues=new JSONArray();
		
		subject=subject.replace("Enquiry for ", "");
		subject=subject.split("\\(")[0];
		activeTask.setCampaign(subject);
		for(String key:keys) {
			
			String regex="";
			if(!key.equalsIgnoreCase("Search Date & Time"))
				if(key.equalsIgnoreCase("full_name")) {
					regex="(Mr Vikas\\(Proprietor\\)|Mr Rahul\\(Chairman\\)[^\\w]*Mr Vikas\\(Owner\\)|Mr Rahul Singh\\(Chairman\\))((?:\\S*\\s*\\n?){1,10})";
				}else
					regex= "(User[^\\w]*"+key+"[^\\w])((?:\\S*\\s*\\n?){1,6})";
			else {
				regex="(Search[^\\w]*Date[^\\w]*&[^\\w]*Time[^\\w])((?:\\S*\\s*\\n?){1,6})";
			}
			Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);
	        Matcher matcher = pattern.matcher(content);
	        System.out.println("---------------------------");
	        
	        while (matcher.find()) {
	        	if(!key.equalsIgnoreCase("requirement")) {
	        		if(key.equals("area")) {
	        			try {
	        				activeTask.setArea(matcher.group(2).replace(":", "").split("User")[0]);
	        			}catch(Exception e) {
	        				e.printStackTrace();
	        			}
	        		}
	        		else if(key.equals("full_name")) {
	        			try {
	        				System.out.println(key+" :"+ matcher.group(2));
	        				JSONObject leadName=new JSONObject();
	        				leadName.put("name", "full_name");
	        				String values[]=new  String[1];
	        				if(matcher.group(2).contains("inquired")) {
	        					values[0]=matcher.group(2).replace(":", "").split("inquired")[0].replace(",","").trim();
	        					activeTask.setLeadName(matcher.group(2).replace(":", "").split("inquired")[0].replace(",","").trim());
	        				}else if(matcher.group(2).contains("enquired")) {
	        					values[0]=matcher.group(2).replace(":", "").split("enquired")[0].replace(",","").trim();
	        					activeTask.setLeadName(matcher.group(2).replace(":", "").split("enquired")[0].replace(",","").trim());
	        				}
	        				leadName.put("values", values);
	        				fieldValues.put(leadName);
	        				System.out.println(key+" :"+ matcher.group(2).replace(":", "").split("User")[0]);
	        			}catch(Exception e) {
	        				e.printStackTrace();
	        			}
	        		}
	        		else if(key.equals("city")) {
	        			try {
	        				String city=matcher.group(2);
	        				if(city.contains("User"))
	        					activeTask.setCity(matcher.group(2).replace(":", "").split("User")[0]);
	        				else if(city.contains("Search"))
	        					activeTask.setCity(matcher.group(2).replace(":", "").split("Search")[0]);
	        			}catch(Exception e) {
	        				e.printStackTrace();
	        			}
	        		}else if(key.equals("state")) {
	        			try {
	        				String state=matcher.group(2);
	        				if(state.contains("User"))
	        					activeTask.setState(matcher.group(2).replace(":", "").split("User")[0]);
	        				else if(state.contains("Search"))
	        					activeTask.setState(matcher.group(2).replace(":", "").split("Search")[0]);
	        			}catch(Exception e) {
	        				e.printStackTrace();
	        			}
	        		}else if(key.equals("phone")) {
	        			try {
	        				JSONObject phoneNumber=new JSONObject();
	        				phoneNumber.put("name", "phone_number");
	        				String values[]=new  String[1];
	        				if(matcher.group(2).contains("User"))
	        					values[0]= matcher.group(2).replace(":", "").split("User")[0].trim();
	        				else {
	        					values[0]=matcher.group(2).replace(":", "").split("View")[0].trim();
	        				}
	        				values[0]=values[0].replaceAll(" ", "");
	        				values[0]=values[0].replaceAll("\\+91", "");
	        				activeTask.setPhoneNumber(values[0]);
	        				List<ActiveTask> existingTask=this.activeTaskService.getTaskByPhoneNumber(values[0]);
	        				try {
	        		    		existingTask.addAll(this.activeTaskService.getTaskByPhoneNumber("+91"+values[0]));
	        		    	}catch (Exception e) {
	        		    		e.printStackTrace();
	        				}
	        				if(existingTask!=null && existingTask.size()>0) {
	        					activeTask.setIsDuplicate(true);
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
	        				phoneNumber.put("values", values);
	        				fieldValues.put(phoneNumber);
	        			}catch(Exception e) {
	        				e.printStackTrace();
	        			}
	        		}else if(key.equals("email")) {
	        			try {
	        				JSONObject email=new JSONObject();
	        				email.put("name", "email");
	        				String values[]=new  String[1];
	        				if(matcher.group(2).contains("User"))
	        					values[0]= matcher.group(2).replace(":", "").split("User")[0].trim();
	        				else if(matcher.group(2).contains("Send")){
	        					values[0]=matcher.group(2).replace(":", "").split("Send")[0].trim();
	        				}else
	        					values[0]=matcher.group(2).replace(":", "").split("View")[0].trim();
	        				email.put("values", values);
	        				try {
	        					taskListener.sendTicketCreationMai(activeTask.getCampaign(), values[0]);
	        				}catch(Exception e3) {
	        					e3.printStackTrace();
	        				}
	        				
	        				fieldValues.put(email);
	        			}catch(Exception e) {
	        				e.printStackTrace();
	        			}
	        		}else if(key.equalsIgnoreCase("Search Date & Time")) {
	        			try {
	        				JSONObject searchDate=new JSONObject();
	        				searchDate.put("name", "Search Date & Time");
	        				String values[]=new String[] {matcher.group(2).replaceFirst(":", "").split("User")[0]};
	        				searchDate.put("values", values);
	        				fieldValues.put(searchDate);
	        			}catch(Exception e) {
	        				e.printStackTrace();
	        			}
	        		}
	        		System.out.println(key+" :"+ matcher.group(2).replaceFirst(":", "").split("User")[0]);
	        	}
	        	else {
	        		if(key.equals("requirement")) {
	        			try {
	        				JSONObject requirement=new JSONObject();
	        				requirement.put("name", "requirement");
	        				String values[]=new String[] {matcher.group(2).replace(":", "").split("Search Date")[0]};
	        				requirement.put("values", values);
	        				fieldValues.put(requirement);
	        				activeTask.setRequirement(values[0]);
	        			}catch(Exception e) {
	        				e.printStackTrace();
	        			}
	        		}
	        		System.out.println(key+" :"+ matcher.group(2).replace(":", "").split("Search Date")[0]);
	        	}
	            
	        }
		}
		FacebookLeads facebookLeads=new FacebookLeads();
		facebookLeads.setFieldData(fieldValues.toString());
		facebookLeads=this.facebookLeadsService.save(facebookLeads);
		activeTask.setFacebookLeads(facebookLeads);
		//activeTask.setAssignee("admin");
		activeTask.setLeadPlatform("J");
		activeTask.setIsActive(true);
		activeTask.setIsClaimed(false);
		activeTask.setTaskGroup("admin");
		activeTask.setTaskName("");
		activeTask.setAssignedTime(LocalDateTime.now().plusHours(5).plusMinutes(30));
		if(activeTask.getAssignee()==null) {
			List<Automation> automationList=automationService.getByIsActive(true);
			if(automationList.size()>0) {
				for(Automation automationParamter:automationList) {
					Map<String,String> userToAllocate =new HashMap<String,String>();
					if(automationParamter.getParamter().equals("Source")) {
						userToAllocate=this.automationBySourceService.allocate(activeTask.getLeadPlatform());
						
					}else if(automationParamter.getParamter().equals("Cousre")) {
						userToAllocate=this.automationByCourseService.allocate(activeTask.getCourse());
						
					}else if(automationParamter.getParamter().equals("Random")) {
						userToAllocate=this.activeTaskService.randomlyAssgin(activeTask, "admin");
					
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
				
			}else {
				activeTask.setAssignee("admin");
			}
		}
		try {
			whatsappService.sendMessage(activeTask.getCampaign(),activeTask.getPhoneNumber());
		}catch(Exception e2) {
			e2.printStackTrace();
		}
		
		this.activeTaskService.save(activeTask);
		
		HistoryEvents hisEvents=new HistoryEvents();
		hisEvents.setActiveTask(activeTask);
		hisEvents.setUserName("Email");
		hisEvents.setUserEmail("Email");
		hisEvents.setUserId("Email");
		hisEvents.setEvent("Task created through email");
		historyEventsService.save(hisEvents);

	}
}