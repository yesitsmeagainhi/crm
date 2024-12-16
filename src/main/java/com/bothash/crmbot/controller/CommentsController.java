package com.bothash.crmbot.controller;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bothash.crmbot.entity.ActiveTask;
import com.bothash.crmbot.entity.Comments;
import com.bothash.crmbot.service.ActiveTaskService;
import com.bothash.crmbot.service.CommentsService;

@RestController
@RequestMapping("/crmbot/comments")
public class CommentsController {

	@Autowired
	private CommentsService commentsService;
	
	@Autowired
	private ActiveTaskService activeTaskService;
	
	@PostMapping("/save/{taskId}")
	public ResponseEntity<Comments> save(@RequestBody Comments comments,@PathVariable Long taskId){
		ActiveTask activeTask=this.activeTaskService.getTaskById(taskId);
		if(activeTask.getIsClaimed()==null || !activeTask.getIsClaimed())
			activeTask.setClaimTime(LocalDateTime.now().plusHours(5).plusMinutes(30));
		
		activeTask.setIsClaimed(true);
		comments.setActiveTask(activeTask);
		Comments saveComment=commentsService.save(comments);
		activeTaskService.save(activeTask);
		return new ResponseEntity<Comments>(saveComment,HttpStatus.OK);
	}
}
