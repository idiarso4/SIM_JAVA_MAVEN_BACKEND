package com.school.sim.dto.response;

import com.school.sim.entity.Attendance;
import com.school.sim.entity.AttendanceStatus;

import java.time.LocalDateTime;

/**
 * Response DTO for attendance information
 */
public class AttendanceResponse {

    private Long id;
    private TeachingActivityInfo teachingActivity;
    private StudentInfo student;
    private AttendanceStatus status;
    private String keterangan;
    private UserInfo recordedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Constructors
    public AttendanceResponse() {}

    public AttendanceResponse(Attendance attendance) {
        this.id = attendance.getId();
        this.status = attendance.getStatus();
        this.keterangan = attendance.getKeterangan();
        this.createdAt = attendance.getCreatedAt();
        this.updatedAt = attendance.getUpdatedAt();

        if (attendance.getTeachingActivity() != null) {
            this.teachingActivity = new TeachingActivityInfo(attendance.getTeachingActivity());
        }

        if (attendance.getStudent() != null) {
            this.student = new StudentInfo(attendance.getStudent());
        }

        if (attendance.getRecordedBy() != null) {
            this.recordedBy = new UserInfo(attendance.getRecordedBy());
        }
    }

    // Static factory method
    public static AttendanceResponse from(Attendance attendance) {
        return new AttendanceResponse(attendance);
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TeachingActivityInfo getTeachingActivity() {
        return teachingActivity;
    }

    public void setTeachingActivity(TeachingActivityInfo teachingActivity) {
        this.teachingActivity = teachingActivity;
    }

    public StudentInfo getStudent() {
        return student;
    }

    public void setStudent(StudentInfo student) {
        this.student = student;
    }

    public AttendanceStatus getStatus() {
        return status;
    }

    public void setStatus(AttendanceStatus status) {
        this.status = status;
    }

    public String getKeterangan() {
        return keterangan;
    }

    public void setKeterangan(String keterangan) {
        this.keterangan = keterangan;
    }

    public UserInfo getRecordedBy() {
        return recordedBy;
    }

    public void setRecordedBy(UserInfo recordedBy) {
        this.recordedBy = recordedBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    /**
     * Inner class for teaching activity information
     */
    public static class TeachingActivityInfo {
        private Long id;
        private String topic;
        private String date;
        private String startTime;
        private String endTime;
        private SubjectInfo subject;
        private ClassRoomInfo classRoom;

        public TeachingActivityInfo() {}

        public TeachingActivityInfo(com.school.sim.entity.TeachingActivity teachingActivity) {
            this.id = teachingActivity.getId();
            this.topic = teachingActivity.getTopic();
            this.date = teachingActivity.getDate().toString();
            this.startTime = teachingActivity.getStartTime().toString();
            this.endTime = teachingActivity.getEndTime().toString();

            if (teachingActivity.getSubject() != null) {
                this.subject = new SubjectInfo(teachingActivity.getSubject());
            }

            if (teachingActivity.getClassRoom() != null) {
                this.classRoom = new ClassRoomInfo(teachingActivity.getClassRoom());
            }
        }

        // Getters and setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getTopic() { return topic; }
        public void setTopic(String topic) { this.topic = topic; }
        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }
        public String getStartTime() { return startTime; }
        public void setStartTime(String startTime) { this.startTime = startTime; }
        public String getEndTime() { return endTime; }
        public void setEndTime(String endTime) { this.endTime = endTime; }
        public SubjectInfo getSubject() { return subject; }
        public void setSubject(SubjectInfo subject) { this.subject = subject; }
        public ClassRoomInfo getClassRoom() { return classRoom; }
        public void setClassRoom(ClassRoomInfo classRoom) { this.classRoom = classRoom; }
    }

    /**
     * Inner class for student information
     */
    public static class StudentInfo {
        private Long id;
        private String nis;
        private String namaLengkap;

        public StudentInfo() {}

        public StudentInfo(com.school.sim.entity.Student student) {
            this.id = student.getId();
            this.nis = student.getNis();
            this.namaLengkap = student.getNamaLengkap();
        }

        // Getters and setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getNis() { return nis; }
        public void setNis(String nis) { this.nis = nis; }
        public String getNamaLengkap() { return namaLengkap; }
        public void setNamaLengkap(String namaLengkap) { this.namaLengkap = namaLengkap; }
    }

    /**
     * Inner class for subject information
     */
    public static class SubjectInfo {
        private Long id;
        private String kodeMapel;
        private String namaMapel;

        public SubjectInfo() {}

        public SubjectInfo(com.school.sim.entity.Subject subject) {
            this.id = subject.getId();
            this.kodeMapel = subject.getKodeMapel();
            this.namaMapel = subject.getNamaMapel();
        }

        // Getters and setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getKodeMapel() { return kodeMapel; }
        public void setKodeMapel(String kodeMapel) { this.kodeMapel = kodeMapel; }
        public String getNamaMapel() { return namaMapel; }
        public void setNamaMapel(String namaMapel) { this.namaMapel = namaMapel; }
    }

    /**
     * Inner class for class room information
     */
    public static class ClassRoomInfo {
        private Long id;
        private String name;
        private Integer grade;

        public ClassRoomInfo() {}

        public ClassRoomInfo(com.school.sim.entity.ClassRoom classRoom) {
            this.id = classRoom.getId();
            this.name = classRoom.getName();
            this.grade = classRoom.getGrade();
        }

        // Getters and setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public Integer getGrade() { return grade; }
        public void setGrade(Integer grade) { this.grade = grade; }
    }

    /**
     * Inner class for user information
     */
    public static class UserInfo {
        private Long id;
        private String name;
        private String email;

        public UserInfo() {}

        public UserInfo(com.school.sim.entity.User user) {
            this.id = user.getId();
            this.name = user.getFirstName() + " " + user.getLastName();
            this.email = user.getEmail();
        }

        // Getters and setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }
}
