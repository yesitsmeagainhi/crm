package com.bothash.crmbot.service.impl;

import java.util.Map;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.bothash.crmbot.service.ActiveTaskService;

@Service
public class WhatsappService {
	
	@Autowired
	private ActiveTaskService activeTaskService;
	
	@Value("${facebook.access.token}")
	private String facebookAccessToken;
	
	@Autowired
	private RestTemplate restTemplate;
	
	public String sendMessage(String taskCampaign,String phonenumber) {
		try {
			
			HttpHeaders header=new HttpHeaders();
			String camaping=taskCampaign;
			camaping=camaping.replaceAll(" ", "_");
			camaping=camaping.toLowerCase();
			header.set("Authorization", "Bearer "+facebookAccessToken);
			header.set("Content-Type", "application/json");
			JSONObject sjon=new JSONObject("{ \"messaging_product\": \"whatsapp\", \"to\": \""+phonenumber+"\", \"type\": \"template\", \"template\": { \"name\": \""+camaping+"\", \"language\": { \"code\": \"en_US\" } } }");
			Map<String, Object> map=sjon.toMap();
			HttpEntity<Map<String, Object>> entity=new HttpEntity<>(map,header);
			ResponseEntity<String> response=this.restTemplate.exchange("https://graph.facebook.com/v13.0/122105275898001273/messages", HttpMethod.POST,entity,String.class);
	           
	           System.out.println(response.getBody());
	           return "sucess";
	          
	       } catch (Exception e) {
	           e.printStackTrace();
	       }
		return "failed";
	}

}
