package com.bothash.crmbot.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bothash.crmbot.entity.Comments;

public interface CommentsRepository extends JpaRepository<Comments, Long>{
	
	List<Comments> findByActiveTaskId(Long id);

}
