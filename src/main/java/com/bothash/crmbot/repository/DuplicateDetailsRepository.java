package com.bothash.crmbot.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bothash.crmbot.entity.DuplicateDetails;

public interface DuplicateDetailsRepository extends JpaRepository<DuplicateDetails, Long>{

}
