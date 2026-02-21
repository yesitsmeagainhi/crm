package com.bothash.crmbot.controller;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

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
	
	// 🔹 Create user
    @PostMapping("/create")
    public ResponseEntity<UserMaster> createUser(@RequestParam String userName,
                                                 @RequestParam String userDisplayName,
                                                 @RequestParam Boolean isActive,
                                                 @RequestParam(required = false) MultipartFile image) throws IOException {
        UserMaster user = new UserMaster();
        user.setUserName(userName);
        user.setUserDisplayName(userDisplayName);
        user.setIsActive(isActive);

        if (image != null && !image.isEmpty()) {
            user.setImage(image.getBytes());
        }

        UserMaster saved = userMasterService.save(user);
        return ResponseEntity.ok(saved);
    }

    // 🔹 Update user
    @PutMapping("/{id}")
    public ResponseEntity<UserMaster> updateUser(@PathVariable Long id,
                                                 @RequestParam String userName,
                                                 @RequestParam String userDisplayName,
                                                 @RequestParam Boolean isActive,
                                                 @RequestParam(required = false) MultipartFile image) throws IOException {
        Optional<UserMaster> userOpt = userMasterService.findById(id);
        if (!userOpt.isPresent()) return ResponseEntity.notFound().build();

        UserMaster user = userOpt.get();
        user.setUserName(userName);
        user.setUserDisplayName(userDisplayName);
        user.setIsActive(isActive);

        if (image != null && !image.isEmpty()) {
            user.setImage(image.getBytes());
        }

        UserMaster updated = userMasterService.save(user);
        return ResponseEntity.ok(updated);
    }

    // 🔹 Delete user
    @DeleteMapping("/delete-image")
    public ResponseEntity<String> deleteUser(@RequestBody LinkedHashMap<String, String> userName) {
    	
    	UserMaster user = userMasterService.getByUserName(userName.get("userName"));
        if(user!=null) {
        	user.setImage(null);
        	userMasterService.save(user);
        }else {
        	return ResponseEntity.notFound().build();
        }
    	
        return ResponseEntity.ok("User deleted");
    }

    // 🔹 Upload or replace image only
    @PostMapping("/image")
    public ResponseEntity<String> uploadImage(@RequestParam String userName,
                                              @RequestParam MultipartFile file) throws IOException {
        UserMaster user = userMasterService.getByUserName(userName);
        if(user == null) return ResponseEntity.notFound().build();

        user.setImage(file.getBytes());
        userMasterService.save(user);

        return ResponseEntity.ok("Image uploaded");
    }

    // 🔹 Get user image
    @GetMapping("/{id}/image")
    public ResponseEntity<byte[]> getImage(@PathVariable Long id) {
        Optional<UserMaster> userOpt = userMasterService.findById(id);
        if (!userOpt.isPresent() || userOpt.get().getImage() == null) {
            return ResponseEntity.notFound().build();
        }

        byte[] image = userOpt.get().getImage();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_JPEG); // adjust based on expected file type
        return new ResponseEntity<>(image, headers, HttpStatus.OK);
    }

    // 🔹 Delete only image
    @DeleteMapping("/{id}/image")
    public ResponseEntity<String> deleteImage(@PathVariable Long id) {
        Optional<UserMaster> userOpt = userMasterService.findById(id);
        if (!userOpt.isPresent()) return ResponseEntity.notFound().build();

        UserMaster user = userOpt.get();
        user.setImage(null);
        userMasterService.save(user);

        return ResponseEntity.ok("Image deleted");
    }

    // 🔹 Get all users
    @GetMapping
    public ResponseEntity<List<UserMaster>> getAllUsers() {
        return ResponseEntity.ok(userMasterService.findAll());
    }

    // 🔹 Get user by ID
    @PostMapping("/get")
    public ResponseEntity<UserMaster> getUserById(@RequestBody LinkedHashMap<String, String> userName) {
        return new ResponseEntity<UserMaster>(userMasterService.getByUserName(userName.get("userName")),HttpStatus.OK);
    }
}
