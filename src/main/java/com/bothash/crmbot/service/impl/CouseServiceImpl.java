package com.bothash.crmbot.service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bothash.crmbot.entity.Course;
import com.bothash.crmbot.repository.CourseRepository;
import com.bothash.crmbot.service.CourseService;

@Service
public class CouseServiceImpl implements CourseService{

	
	@Autowired
	private CourseRepository courseRepository;
	
	@Override
	public List<Course> getAll() {
		return courseRepository.findAll();
	}

	@Override
	public Course getById(Long parameterId) {
		Optional<Course> opt= courseRepository.findById(parameterId);
		if(opt.isPresent()) {
			return opt.get();
		}
		return null;
	}

	@Override
	public Course getByCourseName(String courseName) {
		return courseRepository.findByCourseName(courseName);
	}

}
