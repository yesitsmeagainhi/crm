package com.bothash.crmbot.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bothash.crmbot.entity.Automation;

public interface AutomationRepository extends JpaRepository<Automation, Long>{

	List<Automation> findByIsActiveOrderByPriority(boolean b);

	Automation findByParamter(String paramter);

}
