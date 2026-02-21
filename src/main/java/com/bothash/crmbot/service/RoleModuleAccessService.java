package com.bothash.crmbot.service;
import java.util.List;

import com.bothash.crmbot.entity.RoleModuleAccess;
import com.bothash.crmbot.entity.Modules;

public interface RoleModuleAccessService {
	RoleModuleAccess save(RoleModuleAccess access);

    List<RoleModuleAccess> getByRole(String role);

    RoleModuleAccess getByRoleAndModule(String role, Modules modules);

    boolean hasAccess(String role, Modules modules);

    void deleteAccessByRole(String role);

    List<RoleModuleAccess> getAll();

	void saveBulk(List<RoleModuleAccess> accessList);
	
	boolean checkIfHasAdminAccess(String module,String role);

	boolean checkIfHasAccess(String module,String accessType,String role);
}
