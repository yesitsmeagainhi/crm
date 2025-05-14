package com.bothash.crmbot.service.impl;
import com.bothash.crmbot.configs.TwilioConfig;

import com.twilio.rest.api.v2010.account.Message;
import com.twilio.rest.api.v2010.account.MessageCreator;
import com.twilio.Twilio;
import com.twilio.type.PhoneNumber;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class TwilioWhatsAppService {

    private final TwilioConfig twilioConfig;

    @Autowired
    public TwilioWhatsAppService(TwilioConfig twilioConfig) {
        this.twilioConfig = twilioConfig;
        Twilio.init(twilioConfig.getAccountSid(), twilioConfig.getAuthToken());
    }
    
    @Autowired
    private MessageService messageService;
    
    
    /**
     * Sends a WhatsApp template message to the given recipient.
     * @param to          Customer phone number in E.164 format (without "whatsapp:")
     * @param userName    Example placeholder parameter
     * @param orderNumber Example placeholder parameter
     */

    public String sendWhatsAppMessage(String to, String messageName) {
    com.bothash.crmbot.entity.Message messageDetails = messageService.findByName(messageName);
    String body = "";
    //to = "8169359973";
    if(messageDetails!=null) {
    	body = messageDetails.getText();
    }
	   try {
	        Message message = Message.creator(
	                new PhoneNumber("whatsapp:+91" + to),
	                new PhoneNumber(twilioConfig.getFromNumber()),
	                "HX251fe1a7a96584f061225c8dace65046"
	        ).create();
	        log.debug("Message sent successfully with SID: " + message.getSid());
	        return "Message sent successfully with SID: " + message.getSid();
	    } catch (Exception e) {
	    	log.error("Failed to send message: " + e.getMessage());
	        return "Failed to send message: " + e.getMessage();
	    }
    }
}