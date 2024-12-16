package com.bothash.crmbot.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bothash.crmbot.entity.AutomationUsers;
import com.bothash.crmbot.service.AutomationUserService;

public interface AutomationUsersRepository extends JpaRepository<AutomationUsers, Long>{

	AutomationUsers findByUserIdAndIsActive(String userId,Boolean b);

	List<AutomationUsers> findByIsActive(boolean b);

}
