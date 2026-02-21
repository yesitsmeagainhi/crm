package com.bothash.crmbot.service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bothash.crmbot.entity.RoleModuleAccess;
import com.bothash.crmbot.entity.Modules;
import com.bothash.crmbot.repository.ModulesRepository;
import com.bothash.crmbot.repository.RoleModuleAccessRepository;
import com.bothash.crmbot.service.RoleModuleAccessService;

@Service
public class RoleModuleAccessServiceImpl implements RoleModuleAccessService {

    @Autowired
    private RoleModuleAccessRepository repository;

    
    @Autowired
    private ModulesRepository modulesRepository;

    @Override
    public RoleModuleAccess save(RoleModuleAccess access) {
        return repository.save(access);
    }
    
    public RoleModuleAccess saveOrUpdate(RoleModuleAccess input) {
        Optional<RoleModuleAccess> existing = repository.findByRoleAndModules(input.getRole(), input.getModules());

        RoleModuleAccess record = existing.orElseGet(RoleModuleAccess::new);

        record.setModules(input.getModules());
        record.setRole(input.getRole());
        record.setIsView(input.getIsView());
        record.setIsEdit(input.getIsEdit());
        record.setIsAdminData(input.getIsAdminData());

        return repository.save(record);
    }
    
    @Override
    public void saveBulk(List<RoleModuleAccess> list) {
        for (RoleModuleAccess access : list) {
            saveOrUpdate(access);
        }
    }

    @Override
    public List<RoleModuleAccess> getByRole(String role) {
        return repository.findByRole(role);
    }

    @Override
    public RoleModuleAccess getByRoleAndModule(String role, Modules modules) {
        Optional<RoleModuleAccess> opt = repository.findByRoleAndModules(role, modules);
        if(opt.isPresent()) {
        	return opt.get();
        }
        return null;
    }

    @Override
    public boolean hasAccess(String role, Modules module) {
        return repository.findByRoleAndModules(role, module).isPresent();
    }

    @Override
    public void deleteAccessByRole(String role) {
        repository.deleteByRole(role);
    }

    @Override
    public List<RoleModuleAccess> getAll() {
        return repository.findAll();
    }

	@Override
	public boolean checkIfHasAdminAccess(String module, String role) {
		Modules modules = this.modulesRepository.findByName(module);
		Optional<RoleModuleAccess> opt = this.repository.findByRoleAndModules(role,modules);
		if(opt.isPresent()) {
			RoleModuleAccess access = opt.get();
			if(access.getIsAdminData()!=null && access.getIsAdminData())
				return true;
		}
			
		return false;
	}
	
	@Override
	public boolean checkIfHasAccess(String module,String accessType,String role) {
		Modules modules = this.modulesRepository.findByName(module);
		Optional<RoleModuleAccess> opt = this.repository.findByRoleAndModules(role,modules);
		if(opt.isPresent()) {
			RoleModuleAccess access = opt.get();
			if(accessType.equalsIgnoreCase("EDIT")) {
				return access.getIsEdit();
			}if(accessType.equalsIgnoreCase("VIEW")) {
				return access.getIsView();
			}
		}
			
		return false;
	}
}