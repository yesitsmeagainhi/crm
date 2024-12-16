package com.bothash.crmbot.service;

import java.util.List;
import java.util.Map;

import com.bothash.crmbot.entity.ActiveTask;
import com.bothash.crmbot.entity.AutomationBySource;

public interface AutomationBySourceService {
	
	 Map<String,String> allocate(String courseId);

	List<AutomationBySource> getBySourceId(Long sourceId);

	AutomationBySource save(AutomationBySource automationBySource);

	AutomationBySource getBySourceIdAndGroupId(Long parameterId, String groupId);

	AutomationBySource getByGroupId(String groupId);

	Long delete(Long id);

}
