package com.bothash.crmbot.spec;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.data.jpa.domain.Specification;

import com.bothash.crmbot.dto.FilterRequests;
import com.bothash.crmbot.entity.ActiveTask;
import com.bothash.crmbot.entity.CloseTask;

public class FilterSpecificationClosedTask {

	public static Specification<CloseTask> filter(FilterRequests reportFilterDto) {
		return new Specification<CloseTask>() {
			@Override
			public Predicate toPredicate(Root<CloseTask> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				 query.distinct(true);
				final Collection<Predicate> predicates = new ArrayList<>();
				SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
				
				//if((reportFilterDto.getUserNameForFilterMainTaskPage()!=null && reportFilterDto.getUserNameForFilterMainTaskPage().length()>0 )  (reportFilterDto.get))
				
				if(reportFilterDto.getAssignee()!=null && reportFilterDto.getAssignee().length()>0) {
					final Predicate role= cb.like(cb.lower(root.get("activeTask").get("assignee")),"%"+reportFilterDto.getRole().toLowerCase()+"%");
					predicates.add(role);
					if(  reportFilterDto.getUserNameForFilterMainTaskPage()!=null && reportFilterDto.getUserNameForFilterMainTaskPage().length()>0) {
						final Predicate userName= cb.like(cb.lower(root.get("activeTask").get("owner")),"%"+reportFilterDto.getUserName().toLowerCase()+"%");
						predicates.add(userName);
					}
				}/*if(reportFilterDto.getIsAllTask()!=null && reportFilterDto.getIsAllTask()) {
					if(reportFilterDto.getIsAdmin()!=null && !reportFilterDto.getIsAdmin() ) {
						if(reportFilterDto.getUserName()!=null) {
							if(reportFilterDto.getIsManager()!=null && reportFilterDto.getIsManager()) {
								final Predicate managerTask= cb.like(cb.lower(root.get("managerName")),"%"+reportFilterDto.getUserName().toLowerCase()+"%");
								predicates.add(managerTask);
							}else if(reportFilterDto.getIsTeleCaller()!=null && reportFilterDto.getIsTeleCaller()) {
								final Predicate teleCallerTask= cb.like(cb.lower(root.get("telecallerName")),"%"+reportFilterDto.getUserName().toLowerCase()+"%");
								predicates.add(teleCallerTask);
							}else if(reportFilterDto.getIsTeleCaller()!=null && reportFilterDto.getIsCounsellor()) {
								final Predicate counsellorName= cb.like(cb.lower(root.get("counsellorName")),"%"+reportFilterDto.getUserName().toLowerCase()+"%");
								predicates.add(counsellorName);
							}
							
						}
					}
				}*/
				/*if (reportFilterDto!=null && reportFilterDto.getIsConverted()!=null &&  reportFilterDto.getIsConverted()) {

					final Predicate leadPlatform= cb.equal(cb.lower(root.get("activeTask").get("isConverted")),reportFilterDto.getIsConverted());
					
					predicates.add(leadPlatform);
				}*/
				if (reportFilterDto!=null && reportFilterDto.getLeadPlatform()!=null &&  reportFilterDto.getLeadPlatform().length()>0) {

					final Predicate leadPlatform= cb.equal(cb.lower(root.get("activeTask").get("leadPlatform")),reportFilterDto.getLeadPlatform().toLowerCase());
					
					predicates.add(leadPlatform);
				}
				if (reportFilterDto!=null && reportFilterDto.getCourseName()!=null &&  reportFilterDto.getCourseName().length()>0) {

					final Predicate leadPlatform= cb.equal(cb.lower(root.get("activeTask").get("course")),reportFilterDto.getCourseName().toLowerCase());
					
					predicates.add(leadPlatform);
				}
				/*if(reportFilterDto.getAssignee()!=null && reportFilterDto.getAssignee().length()>0) {
					final Predicate assignee= cb.like(cb.lower(root.get("assignee")),"%"+reportFilterDto.getAssignee().toLowerCase()+"%");
					
					predicates.add(assignee);
					
					if(reportFilterDto.getUserNameForFilterMainTaskPage()!=null && reportFilterDto.getUserNameForFilterMainTaskPage().length()>0) {
						final Predicate ownerForFilter= cb.like(cb.lower(root.get("assignee")),"%"+reportFilterDto.getAssignee().toLowerCase()+"%");
						
						if(reportFilterDto.getAssignee()!=null && reportFilterDto.getAssignee().toLowerCase().equalsIgnoreCase("manager")) {
							final Predicate managerTask= cb.like(cb.lower(root.get("managerName")),"%"+reportFilterDto.getUserNameForFilterMainTaskPage().toLowerCase()+"%");
							predicates.add(managerTask);
						}else if(reportFilterDto.getAssignee()!=null && reportFilterDto.getAssignee().toLowerCase().equalsIgnoreCase("telecaller")) {
							final Predicate teleCallerTask= cb.like(cb.lower(root.get("telecallerName")),"%"+reportFilterDto.getUserNameForFilterMainTaskPage().toLowerCase()+"%");
							predicates.add(teleCallerTask);
						}else if(reportFilterDto.getAssignee()!=null && reportFilterDto.getAssignee().toLowerCase().equalsIgnoreCase("consellor")) {
							final Predicate counsellorName= cb.like(cb.lower(root.get("counsellorName")),"%"+reportFilterDto.getUserNameForFilterMainTaskPage().toLowerCase()+"%");
							predicates.add(counsellorName);
						}
						predicates.add(assignee);
					}
				}*/
				
				if(reportFilterDto.getLeadName()!=null && reportFilterDto.getLeadName().length()>0) {
					try {
						Long phoneNumber=Long.parseLong(reportFilterDto.getPhoneNumber());
					}catch(Exception e) {
						final Predicate leadName= cb.like(root.get("activeTask").get("leadName"),"%"+reportFilterDto.getLeadName().toLowerCase()+"%");
						
						predicates.add(leadName);
					}
					
				}
				if(reportFilterDto.getPhoneNumber()!=null && reportFilterDto.getPhoneNumber().length()>0) {
					try {
						Long phoneNumber=Long.parseLong(reportFilterDto.getPhoneNumber());
						final Predicate phoneNumberPred= cb.like(cb.lower(root.get("activeTask").get("phoneNumber")),"%"+reportFilterDto.getPhoneNumber().toLowerCase()+"%");
						
						predicates.add(phoneNumberPred);
					}catch(Exception e) {
						
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
