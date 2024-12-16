package com.bothash.crmbot.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bothash.crmbot.entity.AutomationByCourse;

public interface AutomationByCourseRepository extends JpaRepository<AutomationByCourse, Long>{

	List<AutomationByCourse> findByIsActiveAndCourseId(Boolean isActive,Long courseId);
	
	AutomationByCourse findByIsLastAllocatedAndCourseId(Boolean isLastAllocated,Long courseId);

	AutomationByCourse findTopByOrderById();

	AutomationByCourse findByCourseIdAndGroupIdAndIsActive(Long parameterId, String groupId, boolean b);

	AutomationByCourse findByGroupIdAndIsActive(String groupId, boolean b);
}
