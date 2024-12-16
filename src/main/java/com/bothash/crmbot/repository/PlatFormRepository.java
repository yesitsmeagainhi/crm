package com.bothash.crmbot.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bothash.crmbot.entity.Platforms;

public interface PlatFormRepository extends JpaRepository<Platforms, Long> {

	Platforms findByName(String platformName);

}
