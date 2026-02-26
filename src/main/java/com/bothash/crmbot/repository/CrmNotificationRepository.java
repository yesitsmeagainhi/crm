package com.bothash.crmbot.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bothash.crmbot.entity.CrmNotification;

public interface CrmNotificationRepository extends JpaRepository<CrmNotification, Long> {

	List<CrmNotification> findByRecipientUserNameAndIsReadFalseOrderByCreatedOnDesc(String recipientUserName);

	List<CrmNotification> findByRecipientUserNameOrderByCreatedOnDesc(String recipientUserName);

	long countByRecipientUserNameAndIsReadFalse(String recipientUserName);
}
