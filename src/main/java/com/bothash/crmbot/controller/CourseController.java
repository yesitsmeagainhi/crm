package com.bothash.crmbot.controller;

import java.security.Principal;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.keycloak.representations.AccessToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import com.bothash.crmbot.dto.Constants;
import com.bothash.crmbot.entity.Automation;
import com.bothash.crmbot.entity.AutomationUsers;
import com.bothash.crmbot.entity.Course;
import com.bothash.crmbot.entity.Message;
import com.bothash.crmbot.entity.UserMaster;
import com.bothash.crmbot.service.CourseService;
import com.bothash.crmbot.service.UserMasterService;

@RestController
@RequestMapping("/course/")
public class CourseController {
	
	@Autowired
	private CourseService courseService;
	
	@Autowired
	private UserMasterService userMasterService;
	
	@ResponseBody
    @GetMapping("coursemaster")
    public ModelAndView listMessages(Principal principal) {
    	
		ModelAndView model = new ModelAndView();
		KeycloakAuthenticationToken token = (KeycloakAuthenticationToken) principal;
		AccessToken accessToken = token.getAccount().getKeycloakSecurityContext().getToken();
		List<Course> courses=this.courseService.getAll();
        model.addObject("courses",courses);
        
        Boolean isAdmin=false;
		Boolean isManager=false;
		Boolean isTelecaller=false;
		Boolean isCounsellor=false;
		String role="";
		
        Set<String> roles=token.getAccount().getRoles();
		String userName=accessToken.getPreferredUsername();
		if(roles.contains("admin") || roles.contains("supervisor")) {
			isAdmin=true;
			role="admin";
			if (roles.contains(Constants.supervisor)) {
				role="supervisor";
			}
		}else if(roles.contains("manager")) {

			role="manager";
			isManager=true;
		}else if(roles.contains("telecaller")) {
			role="telecaller";
			isTelecaller=true;
		}else if(roles.contains("counsellor")) {
			role="counsellor";
			isCounsellor=true;
		}
		model.addObject("role", role);
		model.addObject("userName", accessToken.getName());
		
		UserMaster userDetails=this.userMasterService.getByUserName(userName);
		if(userDetails!=null)
			model.addObject(userName, userDetails);
		else
			model.addObject("isUserActive", true);
		
		model.addObject("prefferedUserName", userName);
		model.addObject("isAdmin", isAdmin);
		model.addObject("isManager", isManager);
		model.addObject("isTelecaller", isTelecaller);
		model.addObject("isCounsellor", isCounsellor);
		model.addObject("messageMaster", true);
		model.setViewName("courses");
        return model;
    }
	
	@GetMapping("/getall")
	public ResponseEntity<List<Course>> getAll(){
		List<Course> courses=this.courseService.getAll();
		return new ResponseEntity<List<Course>>(courses,HttpStatus.OK);
	}
	
	// GET course by ID
    @GetMapping("/{id}")
    public ResponseEntity<Course> getById(@PathVariable Long id) {
        Course  course = courseService.getById(id);
        return course!=null ?ResponseEntity.ok().body(course):ResponseEntity.notFound().build();
    }

    // CREATE new course
    @PostMapping("/create")
    public ResponseEntity<Course> createCourse(@RequestBody Course course) {
        Course created = courseService.create(course);
        return ResponseEntity.status(201).body(created);
    }

    // UPDATE course
    @PutMapping("/{id}")
    public ResponseEntity<Course> updateCourse(@PathVariable Long id, @RequestBody Course course) {
        Optional<Course> updated = courseService.update(id, course);
        return updated.map(ResponseEntity::ok)
                      .orElse(ResponseEntity.notFound().build());
    }

    // DELETE course
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCourse(@PathVariable Long id) {
        boolean deleted = courseService.delete(id);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

}
