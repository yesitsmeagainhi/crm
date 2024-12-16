package com.bothash.crmbot.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bothash.crmbot.entity.UserMaster;

public interface UserMasterRepository extends JpaRepository<UserMaster, Long>{

	UserMaster findByUserName(String userName);

}
