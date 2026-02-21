package com.bothash.crmbot.repository;

import com.bothash.crmbot.entity.CommentMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentMasterRepository extends JpaRepository<CommentMaster, Long> {
    // Add custom queries here if needed
}
