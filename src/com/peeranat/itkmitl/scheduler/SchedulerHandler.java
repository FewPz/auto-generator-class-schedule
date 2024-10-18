package com.peeranat.itkmitl.scheduler;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.peeranat.itkmitl.course.Course;
import com.peeranat.itkmitl.enums.ClassType;
import com.peeranat.itkmitl.enums.DayOfWeek;
import com.peeranat.itkmitl.enums.Major;
import com.peeranat.itkmitl.room.ClassSession;
import com.peeranat.itkmitl.room.Room;
import com.peeranat.itkmitl.room.RoomType;
import com.peeranat.itkmitl.session.TimeSlot;

public class SchedulerHandler {

    private List<Room> rooms = new ArrayList<>();
    private List<Course> courses = new ArrayList<>();
    private Map<Integer, List<ClassSession>> sessionsByYear = new HashMap<>();
    private Map<DayOfWeek, Map<TimeSlot, List<ClassSession>>> schedule = new HashMap<>();

    // กำหนดช่วงเวลาเรียน
    private List<TimeSlot> timeSlots = new ArrayList<>();

    public void initializeData() {
        rooms.add(new Room("M22", RoomType.LECTURE));
        rooms.add(new Room("M23", RoomType.LECTURE));
        rooms.add(new Room("M24", RoomType.LECTURE));
        
        rooms.add(new Room("Lab-207", RoomType.LABORATORY));
        rooms.add(new Room("Lab-205", RoomType.LABORATORY));
        rooms.add(new Room("Lab-203", RoomType.LABORATORY));
        
        rooms.add(new Room("Project-Base 1", RoomType.BOTH));
        rooms.add(new Room("Project-Base 2", RoomType.BOTH));

        // เพิ่มหลักสูตร พร้อมกำหนดชั้นปีของหลักสูตร
        courses.add(new Course("06016481", "COOPERATIVE EDUCATION", true, false, 3, Major.IT));
        courses.add(new Course("06016482", "OVERSEA COOPERATIVE EDUCATION", true, false, 3, Major.IT));

        courses.add(new Course("06016330", "PROJECT IN SOFTWARE ENGINEERING 2", true, false, 4, Major.IT));
        courses.add(new Course("06016340", "PROJECT IN NETWORK AND SYSTEM TECHNOLOGY 2", true, false, 4, Major.IT));
        courses.add(new Course("06016350", "PROJECT IN MULTIMEDIA AND GAME DEVELOPMENT 2", true, false, 4, Major.IT));
        
        courses.add(new Course("06016403", "MULTIMEDIA TECHNOLOGY", true, true, 2, Major.IT));
        courses.add(new Course("06016403", "MULTIMEDIA TECHNOLOGY", true, true, 2, Major.IT));
        courses.add(new Course("06016403", "MULTIMEDIA TECHNOLOGY", true, true, 2, Major.IT));
        
        courses.add(new Course("06016409", "PHYSICAL COMPUTING", true, true, 2, Major.DSBA));
        courses.add(new Course("06016413", "INTRODUCTION TO NETWORK SYSTEMS", true, 2, Major.BIT));

        for (int year = 1; year <= 4; year++) {
            List<ClassSession> sessions = new ArrayList<>();
            for (Course course : courses) {
                if (course.getYear() == year) {
                    if (course.hasLecture()) {
                        sessions.add(new ClassSession(course, ClassType.LECTURE, year));
                    }
                    if (course.hasLaboratory()) {
                        sessions.add(new ClassSession(course, ClassType.LABORATORY, year));
                    }
                }
            }
            sessionsByYear.put(year, sessions);
        }

        // กำหนดช่วงเวลาเรียน
        LocalTime startTime = LocalTime.of(8, 30);
        LocalTime endTime = LocalTime.of(20, 0);

        while (startTime.isBefore(endTime)) {
            LocalTime slotEndTime = startTime.plusHours(1);
            timeSlots.add(new TimeSlot(startTime, slotEndTime));
            startTime = slotEndTime;
        }

        // กำหนดช่วงเวลาพักเที่ยง
        timeSlots.removeIf(ts -> 
                (ts.getStartTime().equals(LocalTime.of(12, 0)) && ts.getEndTime().equals(LocalTime.of(13, 0))) ||
                (ts.getStartTime().equals(LocalTime.of(13, 0)) && ts.getEndTime().equals(LocalTime.of(14, 0))));

        // สร้างตารางเรียนว่างเปล่า
        for (DayOfWeek day : DayOfWeek.values()) {
            schedule.put(day, new HashMap<>());
            for (TimeSlot ts : timeSlots) {
                schedule.get(day).put(ts, new ArrayList<>());
            }
        }
    }

    public void generateSchedule() {
        for (int year = 1; year <= 4; year++) {
            List<ClassSession> sessions = sessionsByYear.get(year);
            // จัดกลุ่มเซสชั่นตามวิชา
            Map<Course, List<ClassSession>> courseSessions = new HashMap<>();
            for (ClassSession session : sessions) {
                courseSessions.computeIfAbsent(session.getCourse(), k -> new ArrayList<>()).add(session);
            }

            for (Course course : courseSessions.keySet()) {
                List<ClassSession> courseSessionList = courseSessions.get(course);
                boolean scheduled = false;
                for (DayOfWeek day : DayOfWeek.values()) {
                    scheduled = scheduleCourseSessions(courseSessionList, day);
                    if (scheduled) {
                        break;
                    }
                }
                if (!scheduled) {
                    System.out.println("ไม่สามารถจัดคาบเรียนสำหรับ " + course.getCourseName() + " ปี " + year);
                }
            }
        }
    }

    private boolean scheduleCourseSessions(List<ClassSession> sessions, DayOfWeek day) {
        Course course = sessions.get(0).getCourse();
        Room lockedRoom = null;  // ห้องเรียนที่ล็อคไว้
        boolean lectureScheduled = false;  // ตรวจสอบว่าคาบทฤษฎีได้ถูกจัดแล้วหรือไม่

        for (ClassSession session : sessions) {
            boolean scheduled = false;

            // จัดคาบทฤษฎีให้ก่อน ถ้าเป็นวิชาทฤษฎีและยังไม่ได้จัดคาบทฤษฎี
            if (session.getClassType() == ClassType.LECTURE && !lectureScheduled) {
                int requiredHours = course.getLectureHours();
                List<TimeSlot> availableTimeSlots = getAvailableTimeSlots(day, session, requiredHours);
                
                // ตรวจสอบห้องว่าง
                if (!availableTimeSlots.isEmpty()) {
                    Room room = (lockedRoom != null) ? lockedRoom : getAvailableRoom(day, availableTimeSlots, ClassType.LECTURE);
                    if (room != null) {
                        lockedRoom = room;  // ล็อกห้องสำหรับทฤษฎี
                        session.schedule(day, availableTimeSlots, room);
                        for (TimeSlot ts : availableTimeSlots) {
                            schedule.get(day).get(ts).add(session);
                        }
                        scheduled = true;
                        lectureScheduled = true;
                    }
                }
            }
            // จัดคาบปฏิบัติหลังจากคาบทฤษฎีเสร็จแล้ว โดยต้องใช้ห้องเดียวกัน
            else if (session.getClassType() == ClassType.LABORATORY && lectureScheduled) {
                int requiredHours = course.getLaboratoryHours();
                List<TimeSlot> availableTimeSlots = getAvailableTimeSlots(day, session, requiredHours);
                
                // ตรวจสอบห้องว่าง และใช้ห้องที่ล็อคไว้แล้ว
                if (!availableTimeSlots.isEmpty()) {
                    Room room = (lockedRoom != null) ? lockedRoom : getAvailableRoom(day, availableTimeSlots, ClassType.LABORATORY);
                    if (room != null) {
                        session.schedule(day, availableTimeSlots, room);
                        for (TimeSlot ts : availableTimeSlots) {
                            schedule.get(day).get(ts).add(session);
                        }
                        scheduled = true;
                    }
                }
            }
            if (!scheduled) {
                return false;
            }
        }
        return true;
    }


    private List<TimeSlot> getAvailableTimeSlots(DayOfWeek day, ClassSession session, int requiredHours) {
        List<TimeSlot> availableSlots = new ArrayList<>();
        int requiredSlots = requiredHours; // แต่ละคาบเรียนยาว 1 ชั่วโมง

        for (int i = 0; i <= timeSlots.size() - requiredSlots; i++) {
            List<TimeSlot> tempSlots = new ArrayList<>();
            boolean slotAvailable = true;
            for (int j = 0; j < requiredSlots; j++) {
                TimeSlot ts = timeSlots.get(i + j);
                if (isDuringLunchBreak(ts)) {
                    slotAvailable = false;
                    break;
                }
                if (!isTimeSlotAvailable(day, ts, session)) {
                    slotAvailable = false;
                    break;
                }
                tempSlots.add(ts);
            }
            if (slotAvailable) {
                availableSlots = tempSlots;
                break;
            }
        }
        return availableSlots;
    }

    
    private boolean isDuringLunchBreak(TimeSlot ts) {
        // ช่วงเวลาพักเที่ยงระหว่าง 12:00 - 14:00
        LocalTime lunchStart = LocalTime.of(12, 0);
        LocalTime lunchEnd = LocalTime.of(14, 0);

        return (ts.getStartTime().isBefore(lunchEnd) && ts.getEndTime().isAfter(lunchStart));
    }


    private boolean isTimeSlotAvailable(DayOfWeek day, TimeSlot ts, ClassSession session) {
        // ตรวจสอบช่วงเวลาพักเที่ยง
        if (isDuringLunchBreak(ts)) {
            return false;
        }

        List<ClassSession> sessionsAtTime = schedule.get(day).get(ts);
        if (sessionsAtTime.size() >= rooms.size()) {
            return false;
        }
        for (ClassSession s : sessionsAtTime) {
            if (s.getYear() == session.getYear()) {
                return false;
            }
        }
        return true;
    }


    private Room getAvailableRoom(DayOfWeek day, List<TimeSlot> timeSlots, ClassType classType) {
        for (Room room : rooms) {
            boolean roomOccupied = false;
            for (TimeSlot ts : timeSlots) {
                List<ClassSession> sessionsAtTime = schedule.get(day).get(ts);
                for (ClassSession s : sessionsAtTime) {
                    if (s.getRoom().getRoomNumber().equals(room.getRoomNumber())) {
                        roomOccupied = true;
                        break;
                    }
                }
                if (roomOccupied) {
                    break;
                }
            }
            if (!roomOccupied) {
                if (isRoomSuitable(room, classType)) {
                    return room;
                }
            }
        }
        return null;
    }

    private boolean isRoomSuitable(Room room, ClassType classType) {
        if (classType == ClassType.LECTURE) {
            return room.getRoomType() == RoomType.LECTURE || room.getRoomType() == RoomType.BOTH;
        } else if (classType == ClassType.LABORATORY) {
            return room.getRoomType() == RoomType.LABORATORY || room.getRoomType() == RoomType.BOTH;
        }
        return false;
    }

    public void printSchedule() {
        for (DayOfWeek day : DayOfWeek.values()) {
            System.out.println("=== " + day + " ===");
            Map<TimeSlot, List<ClassSession>> dailySchedule = schedule.get(day);
            List<TimeSlot> sortedTimeSlots = new ArrayList<>(dailySchedule.keySet());
            sortedTimeSlots.sort(Comparator.comparing(TimeSlot::getStartTime));

            for (TimeSlot ts : sortedTimeSlots) {
                List<ClassSession> sessionsAtTime = dailySchedule.get(ts);
                if (!sessionsAtTime.isEmpty()) {
                    for (ClassSession session : sessionsAtTime) {
                        if (session.getTimeSlots().get(0).equals(ts)) {
                            System.out.println(session);
                        }
                    }
                }
            }
            System.out.println();
        }
    }
}
