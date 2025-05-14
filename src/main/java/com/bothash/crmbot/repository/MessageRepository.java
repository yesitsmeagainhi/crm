package com.bothash.crmbot.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bothash.crmbot.entity.Message;

public interface MessageRepository extends JpaRepository<Message, Long>{

	Message findByMessageName(String name);

}
