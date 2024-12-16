package com.bothash.crmbot.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bothash.crmbot.entity.UserMaster;
import com.bothash.crmbot.service.UserMasterService;

@RestController
@RequestMapping("/usermaster")
public class UserMasterController {

	@Autowired
	private UserMasterService userMasterService;
	
	@PutMapping("/save")
	public ResponseEntity<UserMaster> save(@RequestBody UserMaster userMaster){
		return new ResponseEntity<UserMaster>(this.userMasterService.save(userMaster),HttpStatus.OK);
	}
}
