package com.bothash.crmbot.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bothash.crmbot.entity.Course;

public interface CourseRepository extends JpaRepository<Course, Long>{

	Course findByCourseName(String courseName);

}
