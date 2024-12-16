package com.bothash.crmbot.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;

import com.bothash.crmbot.entity.ActiveTask;
import com.bothash.crmbot.entity.CloseTask;

public interface CloseTaskRepository extends JpaRepository<CloseTask, Long>{

	CloseTask findByActiveTaskId(Long taskId);

	Page<CloseTask> findByIsConverted(boolean b, Pageable requestedPage);

	Page<CloseTask> findByIsConverted(Specification<ActiveTask> filter, boolean b, Pageable requestedPage);

	Page<CloseTask> findAll(Specification<CloseTask> specification, Pageable requestedPage);

	Page<CloseTask> findByIsConvertedAndActiveTaskManagerName(boolean b, String userName, Pageable requestedPage);

	Page<CloseTask> findByIsConvertedAndActiveTaskTelecallerName(boolean b, String userName, Pageable requestedPage);

	Page<CloseTask> findByIsConvertedAndActiveTaskCounsellorName(boolean b, String userName, Pageable requestedPage);

}
