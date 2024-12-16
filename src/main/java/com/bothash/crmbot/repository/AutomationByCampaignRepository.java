package com.bothash.crmbot.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bothash.crmbot.entity.AutomationByCampaign;

public interface AutomationByCampaignRepository extends JpaRepository<AutomationByCampaign, Long>{

	AutomationByCampaign findByFacebookLeadConfigsIdAndGroupId(Long campaignId, String groupId);

	List<AutomationByCampaign> findByFacebookLeadConfigsId(Long id);

	List<AutomationByCampaign> findByIsActiveAndFacebookLeadConfigsId(boolean b, Long id);

}
