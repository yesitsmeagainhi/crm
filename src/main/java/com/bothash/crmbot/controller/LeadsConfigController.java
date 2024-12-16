package com.bothash.crmbot.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bothash.crmbot.entity.FacebookLeadConfigs;
import com.bothash.crmbot.service.FacebookLeadConfigService;

@RestController
@RequestMapping("/admin/")
public class LeadsConfigController {

	@Autowired
	private FacebookLeadConfigService facebookLeadConfigService;
	
	@GetMapping("/facebook/congifs")
	public ResponseEntity<List<FacebookLeadConfigs>> getFacebookCongifs(){
		List<FacebookLeadConfigs> facebookLeadConfigs= facebookLeadConfigService.getAll();
		return new ResponseEntity<List<FacebookLeadConfigs>>(facebookLeadConfigs,HttpStatus.OK);
	}
}
