package com.peeranat.itkmitl.room;

import com.peeranat.itkmitl.course.Course;
import com.peeranat.itkmitl.enums.ClassType;
import com.peeranat.itkmitl.enums.DayOfWeek;
import com.peeranat.itkmitl.session.TimeSlot;
import java.util.List;

public class ClassSession {
	
    private Course course;
    private ClassType classType;
    private DayOfWeek day;
    private List<TimeSlot> timeSlots;
    private Room room;
    private int year;
    private int group;

    public ClassSession(Course course, ClassType classType, int year) {
        this.course = course;
        this.classType = classType;
        this.year = year;
        this.group = 1;
    }

    public void schedule(DayOfWeek day, List<TimeSlot> timeSlots, Room room) {
        this.day = day;
        this.timeSlots = timeSlots;
        this.room = room;
    }

    public Course getCourse() {
        return course;
    }

    public ClassType getClassType() {
        return classType;
    }

    public DayOfWeek getDay() {
        return day;
    }

    public List<TimeSlot> getTimeSlots() {
        return timeSlots;
    }

    public Room getRoom() {
        return room;
    }

    public int getYear() {
        return year;
    }

    public int getGroup() {
        return group;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        sb.append(day);
        sb.append(" ");
        sb.append(timeSlots.get(0).getStartTime());
        sb.append(" - ");
        sb.append(timeSlots.get(timeSlots.size() - 1).getEndTime());
        sb.append("] ปี ");
        sb.append(year);
        sb.append(" - ");
        sb.append(course.getCourseName());
        sb.append(" (");
        sb.append(classType);
        sb.append(") ที่ห้อง ");
        sb.append(room.getRoomNumber());
        return sb.toString();
    }
}
