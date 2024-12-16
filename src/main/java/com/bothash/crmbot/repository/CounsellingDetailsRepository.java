package com.bothash.crmbot.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bothash.crmbot.entity.CounsellingDetails;

public interface CounsellingDetailsRepository extends JpaRepository<CounsellingDetails, Long>{

	List<CounsellingDetails> findByActiveTaskId(Long id);

}
