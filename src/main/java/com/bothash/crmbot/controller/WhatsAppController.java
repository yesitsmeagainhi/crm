package com.bothash.crmbot.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.bothash.crmbot.dto.WhatsAppRequest;
import com.bothash.crmbot.service.impl.TwilioWhatsAppService;

@RestController
@RequestMapping("/api/whatsapp")
public class WhatsAppController {

	@Autowired
    private TwilioWhatsAppService twilioWhatsAppService;

    @PostMapping("/send")
    public String sendWhatsAppMessage(@RequestBody WhatsAppRequest whatsAppRequest) {
        return twilioWhatsAppService.sendWhatsAppMessage(whatsAppRequest.getTo(), whatsAppRequest.getMessage() );
    }
}