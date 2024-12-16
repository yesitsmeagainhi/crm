package com.bothash.crmbot.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bothash.crmbot.entity.Comments;
import com.bothash.crmbot.repository.CommentsRepository;
import com.bothash.crmbot.service.CommentsService;

@Service
public class CommentsServiceImpl implements CommentsService{

	@Autowired
	private CommentsRepository commentsRepository;
	
	@Override
	public List<Comments> getByActiveTask(Long id) {
		return commentsRepository.findByActiveTaskId(id);
	}

	@Override
	public Comments save(Comments comments) {
		return commentsRepository.save(comments);
	}

}
