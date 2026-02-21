package com.bothash.crmbot.service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bothash.crmbot.entity.Platforms;
import com.bothash.crmbot.repository.PlatFormRepository;
import com.bothash.crmbot.service.PlatformService;

@Service
public class PlatformServiceImpl implements PlatformService{

	@Autowired
	private PlatFormRepository platFormRepository;
	
	
	@Override
	public List<Platforms> getAll() {
		return platFormRepository.findAll();
	}


	@Override
	public Platforms getById(Long parameterId) {
		Optional<Platforms> opt= platFormRepository.findById(parameterId);
		if(opt.isPresent()) {
			return opt.get();
		}
		return null;
	}


	@Override
	public Platforms getBySourceName(String platformName) {
		return platFormRepository.findByName(platformName);
	}
	
	@Override
    public Platforms create(Platforms platform) {
        return platFormRepository.save(platform);
    }

    @Override
    public Optional<Platforms> update(Long id, Platforms platform) {
        return platFormRepository.findById(id).map(existing -> {
            existing.setName(platform.getName());
            return platFormRepository.save(existing);
        });
    }

    @Override
    public boolean delete(Long id) {
        if (platFormRepository.existsById(id)) {
            platFormRepository.deleteById(id);
            return true;
        }
        return false;
    }

}
