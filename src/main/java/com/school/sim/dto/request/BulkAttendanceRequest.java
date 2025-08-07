package com.school.sim.dto.request;

import com.school.sim.entity.AttendanceStatus;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import java.util.List;


/**
 * Request DTO for bulk attendance recording
 */
public class BulkAttendanceRequest {

    @NotNull(message = "Teaching activity ID is required")
    private Long teachingActivityId;

    @NotEmpty(message = "Student attendance records are required")
    private List<StudentAttendanceRecord> studentAttendances;

    private Long recordedBy;

    // Constructors
    public BulkAttendanceRequest() {}

    public BulkAttendanceRequest(Long teachingActivityId, List<StudentAttendanceRecord> studentAttendances) {
        this.teachingActivityId = teachingActivityId;
        this.studentAttendances = studentAttendances;
    }

    // Getters and Setters
    public Long getTeachingActivityId() {
        return teachingActivityId;
    }

    public void setTeachingActivityId(Long teachingActivityId) {
        this.teachingActivityId = teachingActivityId;
    }

    public List<StudentAttendanceRecord> getStudentAttendances() {
        return studentAttendances;
    }

    public void setStudentAttendances(List<StudentAttendanceRecord> studentAttendances) {
        this.studentAttendances = studentAttendances;
    }

    public Long getRecordedBy() {
        return recordedBy;
    }

    public void setRecordedBy(Long recordedBy) {
        this.recordedBy = recordedBy;
    }

    /**
     * Inner class for individual student attendance record
     */
    public static class StudentAttendanceRecord {
        @NotNull(message = "Student ID is required")
        private Long studentId;

        @NotNull(message = "Attendance status is required")
        private AttendanceStatus status;

        private String keterangan;

        // Constructors
        public StudentAttendanceRecord() {}

        public StudentAttendanceRecord(Long studentId, AttendanceStatus status) {
            this.studentId = studentId;
            this.status = status;
        }

        public StudentAttendanceRecord(Long studentId, AttendanceStatus status, String keterangan) {
            this.studentId = studentId;
            this.status = status;
            this.keterangan = keterangan;
        }

        // Getters and Setters
        public Long getStudentId() {
            return studentId;
        }

        public void setStudentId(Long studentId) {
            this.studentId = studentId;
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
    }
}
