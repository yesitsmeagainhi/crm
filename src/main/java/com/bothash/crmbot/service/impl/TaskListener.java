package com.bothash.crmbot.service.impl;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bothash.crmbot.configs.BeanUtil;
import com.bothash.crmbot.configs.MailSender;
import com.bothash.crmbot.entity.FacebookLeadConfigs;
import com.bothash.crmbot.service.FacebookLeadConfigService;

@Service
public class TaskListener {
	
	@Autowired
	private MailSender mailSender;
	
	@Autowired
	private FacebookLeadConfigService facebookLeadConfigService;
	
	public void sendTicketCreationMai(String string,String toMail) {
		try {
			String message="<div style=\"text-align:justify;\"> Dear Sir/Madam, </div>"+
					"<div style=\"text-align:justify;\">We received your enquiry</div>"+
					//"<div style=\"text-align:justify;\">Bg-draft link "+"<a href=\""+webportalUrl+"/surepeople/bg-draft?draft="+string+"\">Bg-Draft</a>"+"</div>"+
					"<div style=\"text-align:justify;\"> </div>"+
					"<div style=\"text-align:justify;\"> </div>"+
					"<div style=\"text-align:justify;\"> </div>"+
					//"<div style=\"text-align:justify;\"> For any further assistance, please contact us at sureask@impactsure.com. </div>"+
					"<div style=\"text-align:justify;\"> Thank You </div>";
			FacebookLeadConfigs facebookConfig=facebookLeadConfigService.getByCampaignName(string);
			if(facebookConfig!=null) {
				String tempMessage=facebookConfig.getMessage();
				if(tempMessage.length()>0) {
					message="";
					String[] spltedmessage=tempMessage.split("\n");
					for(int splitIndex=0;splitIndex<spltedmessage.length;splitIndex++) {
						message+="<div style=\"text-align:justify;\">"+spltedmessage[splitIndex]+"</div>";
					}
					message+="<div style=\"text-align:justify;\"> Thank You </div>";
				}
			}
			mailSender = BeanUtil.getBean(MailSender.class);
			Map<String, String> map = new HashMap<String, String>();
			map.put("emailTo", toMail);
			//System.out.println((String) delegateTask.getVariable("ticketStatus"));
			map.put("emailSubject", "Enquiry submitted successfully");
			map.put("emailBody",
					message);
					//"<div style=\"text-align:justify;\"> Team  </div>");
			mailSender.sendMail(map);
			}
			catch (Exception e) {
				e.printStackTrace();
				System.out.println("Unable to send mail.");
			}
	}

}
