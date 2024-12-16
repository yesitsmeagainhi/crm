package com.bothash.crmbot.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bothash.crmbot.entity.FacebookLeadConfigs;

public interface FacebookLeadConfigRepository extends JpaRepository<FacebookLeadConfigs, Long>{

	List<FacebookLeadConfigs> findByIsActive(boolean b);

	List<FacebookLeadConfigs> findByIsActiveAndPlatform(boolean b, String string);

	FacebookLeadConfigs findByIsActiveAndCampaignName(boolean b, String campaignName);

	List<FacebookLeadConfigs> findAllByIsActive(Boolean isActive);

}
