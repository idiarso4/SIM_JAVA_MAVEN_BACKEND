package com.school.sim.dto.request;

import com.school.sim.entity.AttendanceStatus;
import javax.validation.constraints.Size;

/**
 * Request DTO for updating attendance record
 */
public class UpdateAttendanceRequest {

    private AttendanceStatus status;

    @Size(max = 500, message = "Notes must not exceed 500 characters")
    private String keterangan;

    private Long recordedBy;

    // Constructors
    public UpdateAttendanceRequest() {}

    public UpdateAttendanceRequest(AttendanceStatus status, String keterangan) {
        this.status = status;
        this.keterangan = keterangan;
    }

    // Getters and Setters
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
