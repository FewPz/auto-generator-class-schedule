package com.peeranat.itkmitl.course;

import com.peeranat.itkmitl.enums.Major;

public class Course {

    private String courseCode;
    private String courseName;
    private boolean hasLecture;
    private boolean hasLaboratory;
    private int year;
    private Major major; 
    
    private int lectureHours;    
    private int laboratoryHours;

    public Course(String courseCode, String courseName, boolean hasLecture, boolean hasLaboratory, int year, Major major) {
        this.courseCode = courseCode;
        this.courseName = courseName;
        this.hasLecture = hasLecture;
        this.hasLaboratory = hasLaboratory;
        this.year = year;
        this.major = major;
        
        if (hasLecture && !hasLaboratory) {
            this.lectureHours = 3;     
        } else if (hasLecture && hasLaboratory) {
            this.lectureHours = 2;     
            this.laboratoryHours = 2; 
        } 
    }

    public Course(String courseCode, String courseName, boolean hasLecture, int year, Major major) {
        this(courseCode, courseName, hasLecture, false, year, major);
    }

    public String getCourseCode() {
        return courseCode;
    }

    public String getCourseName() {
        return courseName;
    }

    public boolean hasLecture() {
        return hasLecture;
    }

    public boolean hasLaboratory() {
        return hasLaboratory;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public Major getMajor() {
        return major;
    }

    public void setMajor(Major major) {
        this.major = major;
    }
    
    public int getLaboratoryHours() {
		return laboratoryHours;
	}
    
    public int getLectureHours() {
		return lectureHours;
	}
    
    public void setLaboratoryHours(int laboratoryHours) {
		this.laboratoryHours = laboratoryHours;
	}
    
    public void setLectureHours(int lectureHours) {
		this.lectureHours = lectureHours;
	}
}
