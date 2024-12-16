package com.bothash.crmbot.service;

import java.util.List;
import java.util.Map;

import com.bothash.crmbot.entity.ActiveTask;
import com.bothash.crmbot.entity.AutomationByCourse;

public interface AutomationByCourseService {

	 Map<String,String> allocate(String courseId);

	List<AutomationByCourse> getByCourseId(Long courseId);

	AutomationByCourse save(AutomationByCourse automationByCourse);


	AutomationByCourse getByCourseIdAndGroupId(Long parameterId, String groupId);

	AutomationByCourse getByGroupId(String groupId);
	
	Long delete(Long id);
}
