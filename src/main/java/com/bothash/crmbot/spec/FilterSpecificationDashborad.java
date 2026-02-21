package com.bothash.crmbot.spec;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
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
import com.bothash.crmbot.entity.CounsellingDetails;

public class FilterSpecificationDashborad {

	public static Specification<ActiveTask> filter(FilterRequests reportFilterDto) {
		return new Specification<ActiveTask>() {
			@Override
			public Predicate toPredicate(Root<ActiveTask> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				 query.distinct(true);
				final Collection<Predicate> predicates = new ArrayList<>();
				final List<String> usersToIgnore = Arrays.asList("dustbin@gmail.com","vikasji7676@gmail.com");
				SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
				/*if(reportFilterDto.getRole()!=null && reportFilterDto.getRole().length()>0) {
					
					final Predicate role= cb.like(cb.lower(root.get("assignee")),"%"+reportFilterDto.getRole().toLowerCase()+"%");
					predicates.add(role);
					if( reportFilterDto.getUserName()!=null &&  reportFilterDto.getUserName().length()>0) {
						final Predicate userName= cb.like(cb.lower(root.get("owner")),"%"+reportFilterDto.getUserName().toLowerCase()+"%");
						predicates.add(userName);
					}
				}*/
				if(reportFilterDto.getRole()!=null && reportFilterDto.getRole().length()>0 && (reportFilterDto.getUserName()==null ||  reportFilterDto.getUserName().length()<1)) {
					final Predicate assignee= cb.like(cb.lower(root.get("assignee")),"%"+reportFilterDto.getRole().toLowerCase()+"%");
					
					predicates.add(assignee);
				}
				if (reportFilterDto!=null && reportFilterDto.getLeadPlatform()!=null &&  reportFilterDto.getLeadPlatform().length()>0) {

					final Predicate leadPlatform= cb.equal(cb.lower(root.get("leadPlatform")),reportFilterDto.getLeadPlatform().toLowerCase());
					
					predicates.add(leadPlatform);
				}
				if (reportFilterDto!=null && reportFilterDto.getCourseName()!=null &&  reportFilterDto.getCourseName().length()>0) {

					final Predicate leadPlatform= cb.equal(cb.lower(root.get("course")),reportFilterDto.getCourseName().toLowerCase());
					
					predicates.add(leadPlatform);
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
				
					

				if(reportFilterDto.getIsActive()!=null && !(reportFilterDto.getStatus().equalsIgnoreCase("closed") || reportFilterDto.getStatus().equalsIgnoreCase("completed"))) {
					final Predicate isActive = cb.equal(root.get("isActive"),reportFilterDto.getIsActive());
					predicates.add(isActive);
					
				}
				
				if(reportFilterDto.getIsOwner() && reportFilterDto.getUserName()!=null && reportFilterDto.getUserName().length()>0) {
					final Predicate ownerName= cb.like(cb.lower(root.get("owner")),"%"+reportFilterDto.getUserName().toLowerCase()+"%");
					predicates.add(ownerName);
				}else if(reportFilterDto.getUserName()!=null && reportFilterDto.getUserName().length()>0){
					if(reportFilterDto.getRole().equalsIgnoreCase("manager")) {
						final Predicate managerTask= cb.like(cb.lower(root.get("managerName")),"%"+reportFilterDto.getUserName().toLowerCase()+"%");
						predicates.add(managerTask);
					}else if(reportFilterDto.getRole().equalsIgnoreCase("telecaller")) {
						final Predicate teleCallerTask= cb.like(cb.lower(root.get("telecallerName")),"%"+reportFilterDto.getUserName().toLowerCase()+"%");
						predicates.add(teleCallerTask);
					}else if(reportFilterDto.getRole().equalsIgnoreCase("counsellor")) {
						final Predicate counsellorName= cb.like(cb.lower(root.get("counsellorName")),"%"+reportFilterDto.getUserName().toLowerCase()+"%");
						predicates.add(counsellorName);
					}
				}
				
				
				
				if(reportFilterDto.getIsScheduled()!=null) {
					if(reportFilterDto.getIsScheduled()) {
						final Predicate isActive = cb.equal(root.get("isScheduled"),reportFilterDto.getIsScheduled());
						predicates.add(isActive);
					}else {
						final Predicate isActive1 = cb.equal(root.get("isScheduled"),false);
						final Predicate isActive2 = cb.isNull(root.get("isScheduled"));
						final Predicate isActive = cb.or(isActive1,isActive2);
						predicates.add(isActive);
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
					
				}
				
				if(reportFilterDto.getIsSeatConfirmed()!=null) {
					if(reportFilterDto.getIsSeatConfirmed()) {
						final Predicate isSeatConfirmed = cb.equal(root.get("isSeatConfirmed"),reportFilterDto.getIsSeatConfirmed());
						predicates.add(isSeatConfirmed);
					}else {
						final Predicate isSeatConfirmed1 = cb.equal(root.get("isSeatConfirmed"),false);
						final Predicate isSeatConfirmed2 = cb.isNull(root.get("isSeatConfirmed"));
						final Predicate isSeatConfirmed = cb.or(isSeatConfirmed1,isSeatConfirmed2);
						predicates.add(isSeatConfirmed);
					}
					
					
				}
				
				if(reportFilterDto.getFromDate()!=null && reportFilterDto.getFromDate().length()>0) {
					Date startDate=null;
					LocalDateTime startDateNew=null;
					try { 
						startDate = formatter.parse(reportFilterDto.getFromDate().toString()+" 00:01");
						startDateNew=convertToLocalDateTimeViaInstant(startDate);
						startDateNew=startDateNew.minusHours(5).minusMinutes(30);
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					final Predicate fromDate= cb.greaterThanOrEqualTo(root.get("createdOn"),startDateNew);
					
					predicates.add(fromDate);
				}
				if(reportFilterDto.getToDate()!=null && reportFilterDto.getToDate().length()>0) {
					Date endDate=null;
					LocalDateTime endDateNew=null;
					try {
						endDate = formatter.parse(reportFilterDto.getToDate().toString()+" 23:59");
						endDateNew=convertToLocalDateTimeViaInstant(endDate);
						endDateNew=endDateNew.minusHours(5).minusMinutes(30);
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					final Predicate toDate= cb.lessThanOrEqualTo(root.get("createdOn"),endDateNew);
					
					predicates.add(toDate);
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
				if ((reportFilterDto.getIsLeadSummary() != null && reportFilterDto.getIsLeadSummary()) || (reportFilterDto.getIsDashboardFilter()!=null && reportFilterDto.getIsDashboardFilter())) {
				    predicates.add(cb.or(cb.not(root.get("owner").in(usersToIgnore)),cb.isNull(root.get("owner"))));

//					predicates.add(cb.not(root.get("managerName").in(usersToIgnore)));
				}
				
				return cb.and(predicates.toArray(new Predicate[predicates.size()]));
			}
		};
	}
	
	public static LocalDateTime convertToLocalDateTimeViaInstant(Date dateToConvert) {
	    return dateToConvert.toInstant()
	      .atZone(ZoneId.systemDefault())
	      .toLocalDateTime();
	}
}
