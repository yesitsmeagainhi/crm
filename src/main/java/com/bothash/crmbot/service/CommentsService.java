package com.bothash.crmbot.service;

import java.util.List;

import com.bothash.crmbot.entity.Comments;

public interface CommentsService {

	List<Comments> getByActiveTask(Long id);

	Comments save(Comments comments);
}
