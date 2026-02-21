package com.bothash.crmbot.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import javax.transaction.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.bothash.crmbot.entity.ActiveTask;

public interface ActiveTaskRepository extends JpaRepository<ActiveTask, Long>{

	List<ActiveTask> findByAssignee(String role);

	List<ActiveTask> findByAssigneeAndOwner(String role, String userName);

	Page<ActiveTask> findByIsActive(boolean b, Pageable pagerequest);

	Page<ActiveTask> findByAssigneeAndIsActive(String role, boolean b, Pageable pagerequest);

	Page<ActiveTask> findByAssigneeAndOwnerAndIsActive(String role, String userName, boolean b, Pageable pagerequest);
	
	List<ActiveTask> findByAssigneeAndOwnerAndIsActive(String role, String userName, boolean b);

	Page<ActiveTask> findByAssigneeAndIsActive(Specification<ActiveTask> filter, String role, boolean b,
			Pageable requestedPage);

	Page<ActiveTask> findByAssigneeAndOwnerAndIsActive(Specification<ActiveTask> filter, String role,
			String preferredUsername, boolean b, Pageable requestedPage);

	Page<ActiveTask> findByIsActive(Specification<ActiveTask> filter, boolean b, Pageable requestedPage);

	Page<ActiveTask> findAll(Specification<ActiveTask> filter, Pageable requestedPage);

	Page<ActiveTask> findByManagerName(String userName, Pageable requestedPage);

	Page<ActiveTask> findByTelecallerName(String userName, Pageable requestedPage);

	List<ActiveTask> findByIsActive(boolean b);

	List<ActiveTask> findAll(Specification<ActiveTask> filter);

	Long countByIsActive(boolean b);

	Long countByIsConverted(boolean b);

	Long countByCreatedOnGreaterThanEqualAndCreatedOnLessThanEqual(LocalDateTime withSecond, LocalDateTime withSecond2);

	Long countByLeadPlatform(String platform);

	Long countByLeadPlatformAndIsActive(String platform, boolean b);

	Long countByLeadPlatformAndIsConverted(String platform, Boolean isConverted);

	List<ActiveTask> findByPhoneNumber(String phoneNumber);
	
	Long countByOwnerAndIsClaimedAndIsActiveAndCreatedOnGreaterThan(String owner,Boolean isClaimed,Boolean isActive,LocalDateTime time);

	List<ActiveTask> findByAssigneeAndOwnerAndIsActiveAndLeadPlatform(String string, String userId, boolean b,
			String parameter);

	List<ActiveTask> findByAssigneeAndOwnerAndIsActiveAndCourse(String string, String userId, boolean b,
			String parameter);

	Page<ActiveTask> findAllByisScheduled(boolean b, Pageable requestedPage);

	Page<ActiveTask> findAllByisScheduledAndOwner(boolean b, String owner, Pageable requestedPage);

	Page<ActiveTask> findByManagerNameAndAssigneeAndIsActive(String preferredUsername, String string, boolean b,
			Pageable requestedPage);


	Page<ActiveTask> findByManagerNameAndIsScheduled(String preferredUsername, boolean b, Pageable requestedPage);

	Page<ActiveTask> findAllByIsScheduledAndOwner(boolean b, String owner, Pageable requestedPage);

	Page<ActiveTask> findByIsScheduledAndIsActive(boolean b, boolean c, Pageable requestedPage);

	Page<ActiveTask> findAllByIsScheduledAndOwnerAndScheduleTimeLessThanAndScheduleTimeGreaterThan(boolean b,
			String preferredUsername, LocalDateTime tomorrow, LocalDateTime yesterday, Pageable requestedPage);

	Page<ActiveTask> findByManagerNameAndIsScheduledAndScheduleTimeLessThanAndScheduleTimeGreaterThan(
			String preferredUsername, boolean b, LocalDateTime tomorrow, LocalDateTime yesterday,
			Pageable requestedPage);

	
	@Query("select a from ActiveTask a where a.managerName=:preferredUsername and a.isScheduled=:b and (a.scheduleTime > :maxTime or a.scheduleTime < :minTime)" )
	Page<ActiveTask> findByManagerNameAndIsScheduledAndScheduleTimeGreaterThanOrScheduleTimeLessThan(
			@Param("preferredUsername") String preferredUsername,@Param("b") boolean b,@Param("maxTime") LocalDateTime maxTime,@Param("minTime") LocalDateTime minTime, Pageable requestedPage);

	@Query("select a from ActiveTask a where a.owner=:owner and a.isScheduled=:b and (a.scheduleTime > :maxTime or a.scheduleTime < :minTime)" )
	Page<ActiveTask> findAllByIsScheduledAndOwnerAndScheduleTimeGreaterThanOrScheduleTimeLessThan(boolean b,
			String owner, LocalDateTime maxTime, LocalDateTime minTime, Pageable requestedPage);

	Page<ActiveTask> findByIsScheduledAndIsActiveAndScheduleTimeLessThanAndScheduleTimeGreaterThan(boolean b, boolean c,
			LocalDateTime tomorrow, LocalDateTime yesterday, Pageable requestedPage);

	@Query("select a from ActiveTask a where a.isScheduled=:b and a.isActive=:c and (a.scheduleTime > :maxTime or a.scheduleTime < :minTime)" )
	Page<ActiveTask> findByIsScheduledAndIsActiveAndScheduleTimeGreaterThanOrScheduleTimeLessThan(boolean b, boolean c,
			LocalDateTime maxTime, LocalDateTime minTime, Pageable requestedPage);

	Page<ActiveTask> findByManagerNameAndAssigneeAndIsActiveAndIsScheduledAndScheduleTimeLessThanAndScheduleTimeGreaterThan(
			String preferredUsername, String string, boolean b,Boolean isScheduled, LocalDateTime tomorrow, LocalDateTime yesterday,
			Pageable requestedPage);

	@Query("select a from ActiveTask a where a.managerName=:preferredUsername and a.assignee=:string and a.isActive=:b and (a.scheduleTime > :maxTime or a.scheduleTime < :minTime or a.scheduleTime is null)" )
	Page<ActiveTask> findByManagerNameAndAssigneeAndIsActiveAndScheduleTimeGreaterThanOrScheduleTimeLessThan(
			String preferredUsername, String string, boolean b, LocalDateTime maxTime, LocalDateTime minTime,
			Pageable requestedPage2);

	Page<ActiveTask> findByAssigneeAndIsActiveAndIsScheduledAndScheduleTimeLessThanAndScheduleTimeGreaterThan(
			String string, boolean b, boolean c, LocalDateTime tomorrow, LocalDateTime yesterday,
			Pageable requestedPage);
	
	List<ActiveTask> findByAssigneeAndIsActiveAndIsScheduledAndScheduleTimeLessThanAndScheduleTimeGreaterThan(
			String string, boolean b, boolean c, LocalDateTime tomorrow, LocalDateTime yesterday,
			Sort sort);

	@Query("select a from ActiveTask a where   a.assignee=:string and a.isActive=:b and (a.scheduleTime > :maxTime or a.scheduleTime < :minTime or a.scheduleTime is null)" )
	Page<ActiveTask> findByAssigneeAndIsActiveAndScheduleTimeGreaterThanOrScheduleTimeLessThan(String string, boolean b,
			LocalDateTime maxTime, LocalDateTime minTime, Pageable requestedPage2);
	
	@Query("select a from ActiveTask a where   a.assignee=:string and a.isActive=:b and (a.scheduleTime > :maxTime or a.scheduleTime < :minTime or a.scheduleTime is null)" )
	List<ActiveTask> findByAssigneeAndIsActiveAndScheduleTimeGreaterThanOrScheduleTimeLessThan(String string, boolean b,
			LocalDateTime maxTime, LocalDateTime minTime, Sort sort);

	Long countByIsActiveAndOwner(boolean b, String userName);

	List<ActiveTask> findByPhoneNumberAndIsActiveAndCreatedOnGreaterThan(String phoneNumber, boolean b,LocalDateTime time);

	Long countByOwnerAndIsScheduledAndIsActive(String telecallerName, boolean b, boolean c);

	Long countByOwnerAndIsConverted(String telecallerName, boolean b);


	Long countByTelecallerNameAndIsConvertedAndCreatedOnGreaterThan(String telecallerName, boolean b,LocalDateTime time);

	Long countByTelecallerNameAndIsCounsellingDoneAndCreatedOnGreaterThan(String telecallerName, boolean b,LocalDateTime time);
	
	@Modifying
    @Transactional
    @Query(value = "UPDATE active_task " +
            "SET owner = :newOwner, " +
            "    assignee = :newAssignee " +
            "WHERE owner = :oldOwner " +
            "  AND assignee = :oldAssignee " +
            "  AND is_active = true " +
            "  AND course = :course " +
            "  AND lead_platform = :platform " +
            "  AND lead_type = :leadType " +
            "ORDER BY id " +
            "LIMIT :limit", nativeQuery = true)
//    @Query("UPDATE ActiveTask a SET a.owner = :newOwner, a.assignee =:newAssignee  WHERE a.owner = :oldOwner and a.assignee =:oldAssignee and a.isActive=true and a.course=:course and a.leadPlatform =:platform and a.leadType=:leadType LIMIT :limit")
	int  transferLeads(@Param("newOwner") String newOwner,
	        @Param("newAssignee") String newAssignee,
	        @Param("oldOwner") String oldOwner,
	        @Param("oldAssignee") String oldAssignee,
	        @Param("course") String course,
	        @Param("platform") String platform,
	        @Param("leadType") String leadType,
	        @Param("limit") int limit);

	List<ActiveTask> findByAssigneeAndOwnerAndIsActiveAndCourseAndLeadPlatform(String role, String userName, boolean b,
			String course, String platform);

	List<ActiveTask> findByAssigneeAndOwnerAndIsActiveAndCourseAndLeadPlatformAndLeadType(String role, String userName,
			boolean b, String course, String platform, String prospect);

	int count(Specification<ActiveTask> filter);
		
	@Query(value = "SELECT MONTH(schedule_time) AS month, COUNT(*) AS count " +
            "FROM active_task " +
            "WHERE YEAR(schedule_time) = :year AND schedule_time IS NOT NULL " +
            "AND ((owner NOT IN (:excludedOwners) or owner is NULL) )" +
            " AND is_active=true "+
            "GROUP BY MONTH(schedule_time)", nativeQuery = true)
List<Object[]> countTasksPerMonth(@Param("year") int year,@Param("excludedOwners") List<String> excludedOwners);


	@Query(value = "SELECT DAY(schedule_time) AS day, COUNT(*) AS count FROM active_task WHERE YEAR(schedule_time) = :year AND MONTH(schedule_time) = :month "
			+"AND ((owner NOT IN (:excludedOwners) or owner is NULL) )" 
			+" AND is_active=true "
	 +  " GROUP BY DAY(schedule_time)", nativeQuery = true)
	List<Object[]> countTasksPerDay(@Param("year") int year, @Param("month") int month,@Param("excludedOwners") List<String> excludedOwners);
	
	@Query(value = "SELECT YEAR(schedule_time) AS year, COUNT(*) AS count " +
            "FROM active_task " +
            "WHERE schedule_time IS NOT NULL " +
            "AND ((owner NOT IN (:excludedOwners) or owner is NULL)) " +
            " AND is_active=true "+
            "GROUP BY YEAR(schedule_time)", nativeQuery = true)
	List<Object[]> countTasksPerYear(@Param("excludedOwners") List<String> excludedOwners);
	



}
