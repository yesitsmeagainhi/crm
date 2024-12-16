package com.bothash.crmbot.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bothash.crmbot.entity.AutomationBySource;

public interface AutomationBySourceRepository extends JpaRepository<AutomationBySource, Long>{

	AutomationBySource findByIsLastAllocatedAndPlatformsId(Boolean isLastAllocated,Long platformId);
	
	List<AutomationBySource> findByIsActiveAndPlatformsId(Boolean isActive,Long platformId);
	
	AutomationBySource findTopByOrderById();

	AutomationBySource findByPlatformsIdAndGroupIdAndIsActive(Long parameterId, String groupId, boolean b);


	List<AutomationBySource> findByPlatformsIdOrderById(Long long1);

	AutomationBySource findByGroupIdAndIsActive(String groupId, boolean b);
}
