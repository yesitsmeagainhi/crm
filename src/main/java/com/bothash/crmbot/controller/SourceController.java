package com.bothash.crmbot.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bothash.crmbot.entity.Platforms;
import com.bothash.crmbot.service.PlatformService;

@RestController
@RequestMapping("/source/")
public class SourceController {
	
	@Autowired
	private PlatformService platfromService;
	
	@GetMapping("/getall")
	public ResponseEntity<List<Platforms>> getAll(){
		List<Platforms> platfroms=this.platfromService.getAll();
		return new ResponseEntity<List<Platforms>>(platfroms,HttpStatus.OK);
	}

}
