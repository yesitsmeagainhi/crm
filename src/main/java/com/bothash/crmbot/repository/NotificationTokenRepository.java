package com.bothash.crmbot.repository;

import com.bothash.crmbot.entity.NotificationToken;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationTokenRepository extends JpaRepository<NotificationToken, Long> {
    // Optionally, you can add a query method to find a token by user if needed
    NotificationToken findByToken(String token);

	List<NotificationToken> findByUserName(String userName);

	void deleteByUserName(String userName);
}
