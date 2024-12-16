package com.bothash.crmbot.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bothash.crmbot.entity.LastAllocatedUser;

public interface LastAllocatedUserRepository  extends JpaRepository<LastAllocatedUser, Long>{

}
