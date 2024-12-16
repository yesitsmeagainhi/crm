package com.bothash.crmbot.service;

import com.bothash.crmbot.entity.LastAllocatedUser;

public interface LastAllocatedUserService {

	LastAllocatedUser getFirst();

	LastAllocatedUser save(LastAllocatedUser lasUserAllocatedNew);

}
