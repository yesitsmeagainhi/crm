package com.bothash.crmbot.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.bothash.crmbot.entity.Modules;
import com.bothash.crmbot.entity.RoleModuleAccess;

@Repository
public interface RoleModuleAccessRepository extends JpaRepository<RoleModuleAccess, Long> {

    List<RoleModuleAccess> findByRole(String role);


    boolean existsByRoleAndModules(String role, Modules modules);

    void deleteByRole(String role);
    
    Optional<RoleModuleAccess> findByRoleAndModules(String role, Modules modules);
    
}