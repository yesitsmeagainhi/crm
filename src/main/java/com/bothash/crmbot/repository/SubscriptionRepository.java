package com.bothash.crmbot.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bothash.crmbot.entity.PushSubscription;

public interface SubscriptionRepository extends JpaRepository<PushSubscription, Long>{

	PushSubscription findByUserName(String userName);

}
