package com.bothash.crmbot.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bothash.crmbot.entity.Modules;

public interface ModulesRepository extends JpaRepository<Modules, Long> {

	Modules findByName(String module);

}
