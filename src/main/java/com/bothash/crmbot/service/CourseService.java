package com.bothash.crmbot.service;

import java.util.List;
import java.util.Optional;

import com.bothash.crmbot.entity.Course;

public interface CourseService {

	List<Course> getAll();


	Course getByCourseName(String courseName);
	
	Course getById(Long parameterId);
	Course create(Course course);
	Optional<Course> update(Long id, Course course);
	boolean delete(Long id);

	

}
