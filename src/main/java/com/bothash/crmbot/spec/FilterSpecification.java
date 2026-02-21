package com.bothash.crmbot.spec;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import org.springframework.data.jpa.domain.Specification;

import com.bothash.crmbot.dto.FilterRequests;
import com.bothash.crmbot.entity.ActiveTask;
import com.bothash.crmbot.entity.CloseTask;
import com.bothash.crmbot.entity.Comments;
import com.bothash.crmbot.entity.CounsellingDetails;

public class FilterSpecification {

	
	
	public static Specification<ActiveTask> filter(FilterRequests reportFilterDto) {
		return new Specification<ActiveTask>() {
			@Override
			public Predicate toPredicate(Root<ActiveTask> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				 query.distinct(true);
				final Collection<Predicate> predicates = new ArrayList<>();
				SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
				final List<String> usersToIgnore = Arrays.asList("dustbin@gmail.com","vikasji7676@gmail.com");
				if(reportFilterDto.getIsMyTask()!=null && reportFilterDto.getIsMyTask() && reportFilterDto.getRole()!=null) {
					
					final Predicate role= cb.like(cb.lower(root.get("assignee")),"%"+reportFilterDto.getRole().toLowerCase()+"%");
					predicates.add(role);
					if(!reportFilterDto.getIsAdmin() && reportFilterDto.getUserName()!=null ) {
						final Predicate userName= cb.like(cb.lower(root.get("owner")),"%"+reportFilterDto.getUserName().toLowerCase()+"%");
						predicates.add(userName);
					}
				}else if(reportFilterDto.getIsAllTask()!=null && reportFilterDto.getIsAllTask()) {
					if(reportFilterDto.getIsAdmin()!=null && !reportFilterDto.getIsAdmin() ) {
						if(reportFilterDto.getUserName()!=null && reportFilterDto.getUserName().length()>0) {
							if(reportFilterDto.getIsManager()!=null && reportFilterDto.getIsManager()) {
								final Predicate managerTask= cb.like(cb.lower(root.get("managerName")),"%"+reportFilterDto.getUserName().toLowerCase()+"%");
								predicates.add(managerTask);
							}else if(reportFilterDto.getIsTeleCaller()!=null && reportFilterDto.getIsTeleCaller()) {
								final Predicate teleCallerTask= cb.like(cb.lower(root.get("telecallerName")),"%"+reportFilterDto.getUserName().toLowerCase()+"%");
								predicates.add(teleCallerTask);
							}else if(reportFilterDto.getIsCounsellor()!=null && reportFilterDto.getIsCounsellor()) {
								final Predicate counsellorName= cb.like(cb.lower(root.get("counsellorName")),"%"+reportFilterDto.getUserName().toLowerCase()+"%");
								predicates.add(counsellorName);
							}
							
						}
					}
				}
				if (reportFilterDto!=null && reportFilterDto.getLeadType()!=null && !reportFilterDto.getLeadType().equalsIgnoreCase("")) {
					if(!reportFilterDto.getLeadType().equalsIgnoreCase("BLANK")) {
						final Predicate leadType= cb.equal(cb.lower(root.get("leadType")),reportFilterDto.getLeadType().toLowerCase());
						
						predicates.add(leadType);
					}else {
//						final Predicate leadType= cb.equal(cb.lower(root.get("leadType")),reportFilterDto.getLeadType().toLowerCase());
						final Predicate leadType1 = cb.equal(root.get("leadType"),"");
						final Predicate leadType2 = cb.isNull(root.get("leadType"));
						final Predicate leadType = cb.or(leadType1,leadType2);
						predicates.add(leadType);
					}
					
				}
				if (reportFilterDto!=null && reportFilterDto.getLeadPlatform()!=null &&  reportFilterDto.getLeadPlatform().length()>0) {

					final Predicate leadPlatform= cb.equal(cb.lower(root.get("leadPlatform")),reportFilterDto.getLeadPlatform().toLowerCase());
					
					predicates.add(leadPlatform);
				}
				if (reportFilterDto!=null && reportFilterDto.getCourseName()!=null &&  reportFilterDto.getCourseName().length()>0) {

					final Predicate leadPlatform= cb.equal(cb.lower(root.get("course")),reportFilterDto.getCourseName().toLowerCase());
					
					predicates.add(leadPlatform);
				}
				if(reportFilterDto.getAssignee()!=null && reportFilterDto.getAssignee().length()>0) {
					final Predicate assignee= cb.like(cb.lower(root.get("assignee")),"%"+reportFilterDto.getAssignee().toLowerCase()+"%");
					
					predicates.add(assignee);
					
					if(reportFilterDto.getUserNameForFilterMainTaskPage()!=null && reportFilterDto.getUserNameForFilterMainTaskPage().length()>0) {
						final Predicate ownerForFilter= cb.like(cb.lower(root.get("assignee")),"%"+reportFilterDto.getAssignee().toLowerCase()+"%");
						
						if(reportFilterDto.getAssignee()!=null && reportFilterDto.getAssignee().toLowerCase().equalsIgnoreCase("manager")) {
							final Predicate managerTask= cb.like(cb.lower(root.get("managerName")),"%"+reportFilterDto.getUserNameForFilterMainTaskPage().toLowerCase()+"%");
							predicates.add(managerTask);
						}else if(reportFilterDto.getAssignee(	)!=null && reportFilterDto.getAssignee().toLowerCase().equalsIgnoreCase("telecaller")) {
							final Predicate teleCallerTask= cb.like(cb.lower(root.get("telecallerName")),"%"+reportFilterDto.getUserNameForFilterMainTaskPage().toLowerCase()+"%");
							predicates.add(teleCallerTask);
						}else if(reportFilterDto.getAssignee()!=null && reportFilterDto.getAssignee().toLowerCase().equalsIgnoreCase("consellor")) {
							final Predicate counsellorName= cb.like(cb.lower(root.get("counsellorName")),"%"+reportFilterDto.getUserNameForFilterMainTaskPage().toLowerCase()+"%");
							predicates.add(counsellorName);
						}
						predicates.add(assignee);
					}
				}
				if(reportFilterDto.getIsCounselled()!=null) {
//					final Predicate isCounselled1= cb.equal(root.get("isCounsellingDone"),reportFilterDto.getIsCounselled());
//					final Predicate isCounselled2 = cb.and(
//	                        cb.isNotNull(root.get("counsellorName")),
//	                        cb.notEqual(cb.trim(root.get("counsellorName")), "")
//	                    );
//					final Predicate isCounselled = cb.or(isCounselled1,isCounselled2);
					
					Subquery<Long> commentSubquery = query.subquery(Long.class);
                    Root<CounsellingDetails> counsellingRoot = commentSubquery.from(CounsellingDetails.class);
                    
                	commentSubquery.select(cb.count(counsellingRoot))
                    .where(cb.equal(counsellingRoot.get("activeTask"), root));
                    

                    if (reportFilterDto.getIsCounselled()) {
                        predicates.add(cb.greaterThan(commentSubquery.getSelection(), cb.literal(0L)));
                    } else {
                        predicates.add(cb.equal(commentSubquery.getSelection(), cb.literal(0L)));
                    }
//					predicates.add(isCounselled);
					
				}else if(reportFilterDto.getIsLeadTransfer()!=null && reportFilterDto.getIsLeadTransfer() && reportFilterDto.getIsCounselled()!=null && !reportFilterDto.getIsCounselled()) {
					final Predicate isCounselled1 = cb.equal(root.get("isCounsellingDone"),false);
					final Predicate isCounselled4 = cb.equal(root.get("counsellorName"),"");
					final Predicate isCounselled2 = cb.isNull(root.get("isCounsellingDone"));
					final Predicate isCounselled3 = cb.isNull(root.get("counsellorName"));
					final Predicate isCounselled = cb.or(isCounselled1,isCounselled2,isCounselled4,isCounselled3);
					predicates.add(isCounselled);
				}else if(reportFilterDto.getIsCounselled()!=null && !reportFilterDto.getIsCounselled()){
//					final Predicate isCounselled1 = cb.equal(root.get("isCounsellingDone"),false);
					final Predicate isCounselled4 = cb.equal(root.get("counsellorName"),"");
//					final Predicate isCounselled2 = cb.isNull(root.get("isCounsellingDone"));
					final Predicate isCounselled3 = cb.isNull(root.get("counsellorName"));
					final Predicate isCounselled = cb.or(isCounselled4,isCounselled3);
					predicates.add(isCounselled);
				}
				if(reportFilterDto.getLeadName()!=null && reportFilterDto.getLeadName().length()>0) {
					try {
						Long phoneNumber=Long.parseLong(reportFilterDto.getPhoneNumber());
					}catch(Exception e) {
						final Predicate leadName= cb.like(root.get("leadName"),"%"+reportFilterDto.getLeadName().toLowerCase()+"%");
						
						predicates.add(leadName);
					}
					
				}
				if(reportFilterDto.getPhoneNumber()!=null && reportFilterDto.getPhoneNumber().length()>0) {
					try {
						Long phoneNumber=Long.parseLong(reportFilterDto.getPhoneNumber());
						final Predicate phoneNumberPred= cb.like(cb.lower(root.get("phoneNumber")),"%"+reportFilterDto.getPhoneNumber().toLowerCase()+"%");
						
						predicates.add(phoneNumberPred);
					}catch(Exception e) {
						
					}
				}
//				if(reportFilterDto.getFromDate() ==null || reportFilterDto.getFromDate().length()<=0) {
//					LocalDateTime startDateNew=LocalDateTime.of(2024, 1, 1, 0, 0);
//					final Predicate fromDate= cb.greaterThanOrEqualTo(root.get("createdOn"),startDateNew);
//					predicates.add(fromDate);
//				}
//				if(reportFilterDto.getToDate()==null || reportFilterDto.getToDate().length()<=0) {
//					LocalDateTime endDateNew=LocalDateTime.now();
//					final Predicate toDate= cb.lessThanOrEqualTo(root.get("createdOn"),endDateNew);
//					
//					predicates.add(toDate);
//				}
				if(reportFilterDto.getScheduledTime()!=null && reportFilterDto.getIsScheduled()!=null ) {
					if(reportFilterDto.getIsScheduled()) {
						try { 
							LocalDateTime startOfDay = reportFilterDto.getScheduledTime().toLocalDate().atStartOfDay();
						    LocalDateTime endOfDay = reportFilterDto.getScheduledTime().toLocalDate().atTime(23, 59, 59);
						    final Predicate fromDate= cb.greaterThanOrEqualTo(root.get("scheduleTime"),startOfDay);
						    final Predicate tpDate= cb.lessThanOrEqualTo(root.get("scheduleTime"),endOfDay);
						    final Predicate scheduled = cb.and(fromDate,tpDate);
//						    final Predicate fromDate = cb.between(root.get("scheduleTime"), startOfDay, endOfDay);
						    predicates.add(scheduled);
						    
						    final Predicate isScheduled = cb.equal(root.get("isScheduled"),reportFilterDto.getIsScheduled());
							predicates.add(isScheduled);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}else {
						try { 	    
							final Predicate isScheduled1 = cb.equal(root.get("isScheduled"),false);
							final Predicate isScheduled2 = cb.isNull(root.get("isScheduled"));
							final Predicate isScheduled = cb.or(isScheduled1,isScheduled2);
							predicates.add(isScheduled);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					
					
					
				}
				
				if(reportFilterDto.getScheduledTime()!=null && reportFilterDto.getIsScheduledMissed()!=null && reportFilterDto.getIsScheduledMissed()) {
					try { 
						LocalDateTime startOfDay = reportFilterDto.getScheduledTime().toLocalDate().atStartOfDay();
					    final Predicate fromDate= cb.lessThan(root.get("scheduleTime"),startOfDay);
					    predicates.add(fromDate);
					    final Predicate isScheduled = cb.equal(root.get("isScheduled"),reportFilterDto.getIsScheduledMissed());
						predicates.add(isScheduled);
					} catch (Exception e) {
						e.printStackTrace();
					}
					
					
				}
				if(reportFilterDto.getFromDate()!=null && reportFilterDto.getFromDate().length()>0) {
					Date startDate=null;
					LocalDateTime startDateNew=null;
					try { 
						startDate = formatter.parse(reportFilterDto.getFromDate().toString()+" 00:01");
						startDateNew=convertToLocalDateTimeViaInstant(startDate);
						startDateNew=convertIstToGmtAsLocalDateTime(startDateNew);
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					if(reportFilterDto.getDateType()!=null && reportFilterDto.getDateType().length()>0) {
						if(reportFilterDto.getDateType().equalsIgnoreCase("admissionDate")) {
							Subquery<Long> subquery = query.subquery(Long.class);
					        Root<CloseTask> closeTaskRoot = subquery.from(CloseTask.class);
					        subquery.select(closeTaskRoot.get("activeTask").get("id"))
					                .where(
					                    cb.equal(closeTaskRoot.get("activeTask"), root),
					                    cb.greaterThanOrEqualTo(closeTaskRoot.get("createdOn"), startDateNew)
					                );

					        predicates.add(cb.exists(subquery));
						}else if(reportFilterDto.getDateType().equalsIgnoreCase("cousellingDate")){
							Subquery<Long> subquery = query.subquery(Long.class);
					        Root<CounsellingDetails> counsellingRoot = subquery.from(CounsellingDetails.class);
					        subquery.select(counsellingRoot.get("activeTask").get("id"))
					                .where(
					                    cb.equal(counsellingRoot.get("activeTask"), root),
					                    cb.greaterThanOrEqualTo(counsellingRoot.get("createdOn"), startDateNew)
					                );

					        predicates.add(cb.exists(subquery));
						}
					}else {
						final Predicate fromDate= cb.greaterThanOrEqualTo(root.get("createdOn"),startDateNew);
						
						predicates.add(fromDate);
					}
					
				}
				if(reportFilterDto.getToDate()!=null && reportFilterDto.getToDate().length()>0) {
					Date endDate=null;
					LocalDateTime endDateNew=null;
					try {
						endDate = formatter.parse(reportFilterDto.getToDate().toString()+" 23:59");
						endDateNew=convertToLocalDateTimeViaInstant(endDate);
						endDateNew=convertIstToGmtAsLocalDateTime(endDateNew);
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}if(reportFilterDto.getDateType()!=null && reportFilterDto.getDateType().length()>0) {
						if(reportFilterDto.getDateType().equalsIgnoreCase("admissionDate")) {
							Subquery<Long> subquery = query.subquery(Long.class);
					        Root<CloseTask> closeTaskRoot = subquery.from(CloseTask.class);
					        subquery.select(closeTaskRoot.get("activeTask").get("id"))
					                .where(
					                    cb.equal(closeTaskRoot.get("activeTask"), root),
					                    cb.lessThanOrEqualTo(closeTaskRoot.get("createdOn"), endDateNew)
					                );

					        predicates.add(cb.exists(subquery));
						}else if(reportFilterDto.getDateType().equalsIgnoreCase("cousellingDate")){
							Subquery<Long> subquery = query.subquery(Long.class);
					        Root<CounsellingDetails> counsellingRoot = subquery.from(CounsellingDetails.class);
					        subquery.select(counsellingRoot.get("activeTask").get("id"))
					                .where(
					                    cb.equal(counsellingRoot.get("activeTask"), root),
					                    cb.lessThanOrEqualTo(counsellingRoot.get("createdOn"), endDateNew)
					                );

					        predicates.add(cb.exists(subquery));
						}
					}else {
						final Predicate toDate= cb.lessThanOrEqualTo(root.get("createdOn"),endDateNew);
						
						predicates.add(toDate);
					}
				}
				
				if(reportFilterDto.getIsSeatConfirmed()!=null ) {
					if(reportFilterDto.getIsSeatConfirmed()) {
						final Predicate isActive = cb.equal(root.get("isSeatConfirmed"),reportFilterDto.getIsSeatConfirmed());
						predicates.add(isActive);
					}else {
						final Predicate isSeatConfirmed1 = cb.equal(root.get("isSeatConfirmed"),reportFilterDto.getIsSeatConfirmed());
						final Predicate isSeatConfirmed2 = cb.isNull(root.get("isSeatConfirmed"));
						final Predicate isSeatConfirmed = cb.or(isSeatConfirmed1,isSeatConfirmed2);
						predicates.add(isSeatConfirmed);
					}
					
				}
				if(reportFilterDto.getIsConverted()!=null && reportFilterDto.getIsDashboardFilter() !=null && reportFilterDto.getIsDashboardFilter()) {
					
					final Predicate isConverted = cb.equal(root.get("isConverted"),reportFilterDto.getIsConverted());
					predicates.add(isConverted);

					
				}
				if(reportFilterDto.getIsClaimed()!=null ) {
					if(reportFilterDto.getIsClaimed()) {
						final Predicate isClaimed = cb.equal(root.get("isClaimed"),reportFilterDto.getIsClaimed());
						predicates.add(isClaimed);
					}else {
						final Predicate isClaimed1 = cb.equal(root.get("isClaimed"),false);
						final Predicate isClaimed2 = cb.isNull(root.get("isClaimed"));
						final Predicate isClaimed = cb.or(isClaimed1,isClaimed2);
						predicates.add(isClaimed);
					}
				
					

				}
				
				if (reportFilterDto.getHasComments() != null) {
                    Subquery<Long> commentSubquery = query.subquery(Long.class);
                    Root<Comments> commentRoot = commentSubquery.from(Comments.class);
                    if(reportFilterDto.getUserName()!=null) {
                    	commentSubquery.select(cb.count(commentRoot))
                        .where(cb.equal(commentRoot.get("activeTask"), root),
                        		cb.equal(cb.lower(commentRoot.get("userEmail")), reportFilterDto.getUserName().toLowerCase()));
                    }else {
                    	commentSubquery.select(cb.count(commentRoot))
                        .where(cb.equal(commentRoot.get("activeTask"), root));
                    }
                    

                    if (reportFilterDto.getHasComments()) {
                        predicates.add(cb.greaterThan(commentSubquery.getSelection(), cb.literal(0L)));
                    } else {
                        predicates.add(cb.equal(commentSubquery.getSelection(), cb.literal(0L)));
                    }
                }
				
				if (reportFilterDto.getNoComments() != null && reportFilterDto.getNoComments()) {
				    Subquery<Long> commentSubquery = query.subquery(Long.class);
				    Root<Comments> commentRoot = commentSubquery.from(Comments.class);
				    commentSubquery.select(cb.count(commentRoot))
				        .where(cb.equal(commentRoot.get("activeTask"), root));
				    
				    predicates.add(cb.equal(commentSubquery.getSelection(), cb.literal(0L)));
				}

				if (reportFilterDto.getIsDustin() != null && reportFilterDto.getIsDustin()) {
					final Predicate userName= cb.like(cb.lower(root.get("owner")),"%"+"dustbin@gmail.com"+"%");
					predicates.add(userName);
				}
				if ((reportFilterDto.getIsLeadSummary() != null && reportFilterDto.getIsLeadSummary()) || (reportFilterDto.getIsDashboardFilter()!=null && reportFilterDto.getIsDashboardFilter())) {
				    predicates.add(cb.or(cb.not(root.get("owner").in(usersToIgnore)),cb.isNull(root.get("owner"))));

//					predicates.add(cb.not(root.get("managerName").in(usersToIgnore)));
				}
				
				
				if(reportFilterDto.getStatus()!=null) {
					if(reportFilterDto.getStatus().equalsIgnoreCase("open")) {
						final Predicate isClaimed = cb.isNull(root.get("isClaimed"));
						final Predicate isClaimed2 = cb.equal(root.get("isClaimed"),false);
						final Predicate isClaimedMain = cb.or(isClaimed,isClaimed2);
						predicates.add(isClaimedMain);
					}else if(reportFilterDto.getStatus().equalsIgnoreCase("processed")) {
						final Predicate isClaimed = cb.equal(root.get("isClaimed"),true);
						predicates.add(isClaimed);
					}else if(reportFilterDto.getStatus().equalsIgnoreCase("completed")) {
						final Predicate isClaimed = cb.equal(root.get("isConverted"),true);
						final Predicate isActive = cb.equal(root.get("isActive"),false);
						predicates.add(isActive);
						predicates.add(isClaimed);
					}else if(reportFilterDto.getStatus().equalsIgnoreCase("closed")) {
						final Predicate isClaimed = cb.equal(root.get("isConverted"),false);
						predicates.add(isClaimed);
						final Predicate isActive = cb.equal(root.get("isActive"),false);
						predicates.add(isActive);
					}
				}
				
					
				if(reportFilterDto.getDateType()!=null && reportFilterDto.getDateType().equalsIgnoreCase("admissionDate")) {
					final Predicate isActive = cb.equal(root.get("isActive"),false);
					predicates.add(isActive);
				}else {
					if(reportFilterDto.getIsActive()!=null && (reportFilterDto.getStatus()!=null && !(reportFilterDto.getStatus().equalsIgnoreCase("closed") || reportFilterDto.getStatus().equalsIgnoreCase("completed")))) {
						final Predicate isActive = cb.equal(root.get("isActive"),reportFilterDto.getIsActive());
						predicates.add(isActive);
						
					}else if(reportFilterDto.getIsActive()!=null) {
						
						final Predicate isActive = cb.equal(root.get("isActive"),reportFilterDto.getIsActive());
						predicates.add(isActive);

						
					}
				}
				
				
				if(reportFilterDto.getIsOwner()!=null && reportFilterDto.getIsOwner() && reportFilterDto.getUserName()!=null && reportFilterDto.getUserName().length()>0) {
					final Predicate ownerName= cb.like(cb.lower(root.get("owner")),"%"+reportFilterDto.getUserName().toLowerCase()+"%");
					predicates.add(ownerName);
				}else if(reportFilterDto.getUserName()!=null && reportFilterDto.getUserName().length()>0 && reportFilterDto.getRole()!=null){
					if(reportFilterDto.getRole().equalsIgnoreCase("manager")) {
						final Predicate managerTask= cb.like(cb.lower(root.get("managerName")),"%"+reportFilterDto.getUserName().toLowerCase()+"%");
						predicates.add(managerTask);
					}else if(reportFilterDto.getRole().equalsIgnoreCase("telecaller")) {
						final Predicate teleCallerTask= cb.like(cb.lower(root.get("telecallerName")),"%"+reportFilterDto.getUserName().toLowerCase()+"%");
						predicates.add(teleCallerTask);
					}else if(reportFilterDto.getRole().equalsIgnoreCase("counsellor")) {
						final Predicate counsellorName= cb.like(cb.lower(root.get("counsellorName")),"%"+reportFilterDto.getUserName().toLowerCase()+"%");
						predicates.add(counsellorName);
					}else if(reportFilterDto.getIsAdmin()==null || !reportFilterDto.getIsAdmin()) {
						final Predicate counsellorName= cb.like(cb.lower(root.get("owner")),"%"+reportFilterDto.getUserName().toLowerCase()+"%");
						predicates.add(counsellorName);
					}
				}
				return cb.and(predicates.toArray(new Predicate[predicates.size()]));
			}
		};
	}
	
	public static LocalDateTime convertIstToGmtAsLocalDateTime(LocalDateTime istDateTime) {
        // Create ZonedDateTime in IST
        ZonedDateTime istZoned = istDateTime.atZone(ZoneId.of("Asia/Kolkata"));

        // Convert to GMT
        ZonedDateTime gmtZoned = istZoned.withZoneSameInstant(ZoneId.of("GMT"));

        // Return as LocalDateTime in GMT (i.e., time without zone)
        return gmtZoned.toLocalDateTime();
    }
	
	public static LocalDateTime convertToLocalDateTimeViaInstant(Date dateToConvert) {
	    return dateToConvert.toInstant()
	      .atZone(ZoneId.systemDefault())
	      .toLocalDateTime();
	}
}
