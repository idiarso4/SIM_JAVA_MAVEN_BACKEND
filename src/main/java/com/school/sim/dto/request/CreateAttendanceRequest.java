package com.school.sim.dto.request;

import com.school.sim.entity.AttendanceStatus;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * Request DTO for creating attendance record
 */
public class CreateAttendanceRequest {

    @NotNull(message = "Teaching activity ID is required")
    private Long teachingActivityId;

    @NotNull(message = "Student ID is required")
    private Long studentId;

    @NotNull(message = "Attendance status is required")
    private AttendanceStatus status;

    @Size(max = 500, message = "Notes must not exceed 500 characters")
    private String keterangan;

    private Long recordedBy;

    // Constructors
    public CreateAttendanceRequest() {}

    public CreateAttendanceRequest(Long teachingActivityId, Long studentId, AttendanceStatus status) {
        this.teachingActivityId = teachingActivityId;
        this.studentId = studentId;
        this.status = status;
    }

    // Getters and Setters
    public Long getTeachingActivityId() {
        return teachingActivityId;
    }

    public void setTeachingActivityId(Long teachingActivityId) {
        this.teachingActivityId = teachingActivityId;
    }

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

    public Long getRecordedBy() {
        return recordedBy;
    }

    public void setRecordedBy(Long recordedBy) {
        this.recordedBy = recordedBy;
    }
}
