package com.bothash.crmbot.service;

import java.util.List;

import com.bothash.crmbot.entity.Course;

public interface CourseService {

	List<Course> getAll();

	Course getById(Long parameterId);

	Course getByCourseName(String courseName);
	

}
