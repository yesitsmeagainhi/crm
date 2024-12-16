package com.bothash.crmbot.service;

import java.util.List;
import java.util.Map;

import com.bothash.crmbot.dto.FilterRequests;
import com.bothash.crmbot.entity.ActiveTask;

public interface GraphService {

	Map<String, List> generateXAndYPlots(List<ActiveTask> tasks, FilterRequests filterRequests);

}
