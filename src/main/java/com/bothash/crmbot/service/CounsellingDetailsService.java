package com.bothash.crmbot.service;

import java.util.List;

import com.bothash.crmbot.entity.CounsellingDetails;

public interface CounsellingDetailsService {
	CounsellingDetails save(CounsellingDetails counsellingDetails);

	List<CounsellingDetails> getByActiveTask(Long id);

}
