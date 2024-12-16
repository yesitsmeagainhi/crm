package com.bothash.crmbot.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bothash.crmbot.entity.TargetMails;

public interface TargetMailsRepository extends JpaRepository<TargetMails, Long>{

	List<TargetMails> findByIsActive(boolean b);

}
