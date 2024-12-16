package com.bothash.crmbot.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bothash.crmbot.entity.Course;
import com.bothash.crmbot.service.CourseService;

@RestController
@RequestMapping("/course/")
public class CourseController {
	
	@Autowired
	private CourseService courseService;
	
	@GetMapping("/getall")
	public ResponseEntity<List<Course>> getAll(){
		List<Course> courses=this.courseService.getAll();
		return new ResponseEntity<List<Course>>(courses,HttpStatus.OK);
	}

}
