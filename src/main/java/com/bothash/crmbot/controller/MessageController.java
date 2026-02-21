package com.bothash.crmbot.controller;

import java.security.Principal;
import java.util.List;
import java.util.Set;

import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.keycloak.representations.AccessToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.bothash.crmbot.dto.Constants;
import com.bothash.crmbot.entity.Automation;
import com.bothash.crmbot.entity.AutomationUsers;
import com.bothash.crmbot.entity.Message;
import com.bothash.crmbot.entity.RoleModuleAccess;
import com.bothash.crmbot.entity.UserMaster;
import com.bothash.crmbot.service.AutomationService;
import com.bothash.crmbot.service.AutomationUserService;
import com.bothash.crmbot.service.RoleModuleAccessService;
import com.bothash.crmbot.service.UserMasterService;
import com.bothash.crmbot.service.impl.MessageService;

@Controller
@RequestMapping("/messages")
public class MessageController {

    @Autowired
    private MessageService messageService;
    
    @Autowired
	private AutomationService automationService;
    
    @Autowired
	private AutomationUserService automationUserService;
    
    @Autowired
	private UserMasterService userMasterService;
    
    @Autowired
	private RoleModuleAccessService roleModuleAccessService;

    @GetMapping
    public String listMessages(@RequestParam(defaultValue = "1") int page,
                               @RequestParam(defaultValue = "5") int size,
                               Model model,Principal principal) {
    	
    	
		KeycloakAuthenticationToken token = (KeycloakAuthenticationToken) principal;
		AccessToken accessToken = token.getAccount().getKeycloakSecurityContext().getToken();
        Page<Message> messagePage = messageService.getMessages(page, size);
        model.addAttribute("messages", messagePage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", messagePage.getTotalPages());
        
        Boolean isAdmin=false;
		Boolean isManager=false;
		Boolean isTelecaller=false;
		Boolean isCounsellor=false;
		
        Set<String> roles=token.getAccount().getRoles();
        String role = roles.stream().findFirst().orElse(null);
        
		String userName=accessToken.getPreferredUsername();
		Boolean isAutomated=false;
		if(role.equalsIgnoreCase("admin")) {
			List<Automation> autoamtions=this.automationService.getByIsActive(true);
			if(autoamtions.size()>0) {
				isAutomated=true;
			}
			
			
		}else if(role.equalsIgnoreCase("manager")) {
			
			AutomationUsers automationUser=automationUserService.getByUserId(userName);
			if(automationUser!=null) {
				isAutomated=true;
			}
		}
		model.addAttribute("isAutomated", isAutomated);
		model.addAttribute("role", role);
		model.addAttribute("userName", accessToken.getName());
		
		UserMaster userDetails=this.userMasterService.getByUserName(userName);
		if(userDetails!=null)
			model.addAttribute(userName, userDetails);
		else
			model.addAttribute("isUserActive", true);
		
		model.addAttribute("prefferedUserName", userName);
		model.addAttribute("isTelecaller", isTelecaller);
		model.addAttribute("isCounsellor", isCounsellor);
		model.addAttribute("messageMaster", true);
		
		List<RoleModuleAccess> access = this.roleModuleAccessService.getByRole(role);
		model.addAttribute("access", access);
		
        return "messages";
    }

    @PostMapping("/add")
    public String addMessage(@RequestParam String messageName,@RequestParam String text) {
        Message message = new Message();
        message.setMessageName(messageName);
        message.setText(text);
        messageService.saveMessage(message);
        return "redirect:/messages";
    }

    @PostMapping("/update")
    public String updateMessage(@RequestParam Long id,
                                @RequestParam String messageName,
                                @RequestParam String text) {
        messageService.updateMessage(id, messageName, text);
        return "redirect:/messages";
    }


    @GetMapping("/delete/{id}")
    public String deleteMessage(@PathVariable Long id) {
        messageService.deleteMessage(id);
        return "redirect:/messages";
    }
}