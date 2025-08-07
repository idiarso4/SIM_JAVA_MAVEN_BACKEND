package com.school.sim.entity;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * Student entity representing students in the School Information Management System
 * This is a basic implementation that will be expanded in the next task
 */
@Entity
@Table(name = "students", indexes = {
        @Index(name = "idx_student_nis", columnList = "nis"),
        @Index(name = "idx_student_status", columnList = "status")
})
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "NIS is required")
    @Size(max = 20, message = "NIS must not exceed 20 characters")
    @Column(name = "nis", nullable = false, unique = true, length = 20)
    private String nis;

    @NotBlank(message = "Full name is required")
    @Size(max = 100, message = "Name must not exceed 100 characters")
    @Column(name = "nama_lengkap", nullable = false, length = 100)
    private String namaLengkap;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_room_id")
    private ClassRoom classRoom;

    @Column(name = "tempat_lahir", length = 50)
    private String tempatLahir;

    @Column(name = "tanggal_lahir")
    private LocalDate tanggalLahir;

    @Enumerated(EnumType.STRING)
    @Column(name = "jenis_kelamin", length = 10)
    private Gender jenisKelamin;

    @Column(name = "agama", length = 20)
    private String agama;

    @Column(name = "alamat", columnDefinition = "TEXT")
    private String alamat;

    @Column(name = "nama_ayah", length = 100)
    private String namaAyah;

    @Column(name = "nama_ibu", length = 100)
    private String namaIbu;

    @Column(name = "pekerjaan_ayah", length = 50)
    private String pekerjaanAyah;

    @Column(name = "pekerjaan_ibu", length = 50)
    private String pekerjaanIbu;

    @Column(name = "no_hp_ortu", length = 20)
    private String noHpOrtu;

    @Column(name = "alamat_ortu", columnDefinition = "TEXT")
    private String alamatOrtu;

    @Column(name = "tahun_masuk")
    private Integer tahunMasuk;

    @Column(name = "asal_sekolah", length = 100)
    private String asalSekolah;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private StudentStatus status = StudentStatus.ACTIVE;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Attendance> attendances;
    
    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<StudentAssessment> assessments;
    
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "student_extracurricular_activities",
        joinColumns = @JoinColumn(name = "student_id"),
        inverseJoinColumns = @JoinColumn(name = "extracurricular_activity_id")
    )
    private List<ExtracurricularActivity> extracurricularActivities;

    // Constructors
    public Student() {
    }

    public Student(String nis, String namaLengkap) {
        this.nis = nis;
        this.namaLengkap = namaLengkap;
    }

    // Lifecycle callbacks
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNis() {
        return nis;
    }

    public void setNis(String nis) {
        this.nis = nis;
    }

    public String getNamaLengkap() {
        return namaLengkap;
    }

    public void setNamaLengkap(String namaLengkap) {
        this.namaLengkap = namaLengkap;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public ClassRoom getClassRoom() {
        return classRoom;
    }

    public void setClassRoom(ClassRoom classRoom) {
        this.classRoom = classRoom;
    }

    public String getTempatLahir() {
        return tempatLahir;
    }

    public void setTempatLahir(String tempatLahir) {
        this.tempatLahir = tempatLahir;
    }

    public LocalDate getTanggalLahir() {
        return tanggalLahir;
    }

    public void setTanggalLahir(LocalDate tanggalLahir) {
        this.tanggalLahir = tanggalLahir;
    }

    public Gender getJenisKelamin() {
        return jenisKelamin;
    }

    public void setJenisKelamin(Gender jenisKelamin) {
        this.jenisKelamin = jenisKelamin;
    }

    public String getAgama() {
        return agama;
    }

    public void setAgama(String agama) {
        this.agama = agama;
    }

    public String getAlamat() {
        return alamat;
    }

    public void setAlamat(String alamat) {
        this.alamat = alamat;
    }

    public StudentStatus getStatus() {
        return status;
    }

    public void setStatus(StudentStatus status) {
        this.status = status;
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

    public String getNamaAyah() {
        return namaAyah;
    }

    public void setNamaAyah(String namaAyah) {
        this.namaAyah = namaAyah;
    }

    public String getNamaIbu() {
        return namaIbu;
    }

    public void setNamaIbu(String namaIbu) {
        this.namaIbu = namaIbu;
    }

    public String getPekerjaanAyah() {
        return pekerjaanAyah;
    }

    public void setPekerjaanAyah(String pekerjaanAyah) {
        this.pekerjaanAyah = pekerjaanAyah;
    }

    public String getPekerjaanIbu() {
        return pekerjaanIbu;
    }

    public void setPekerjaanIbu(String pekerjaanIbu) {
        this.pekerjaanIbu = pekerjaanIbu;
    }

    public String getNoHpOrtu() {
        return noHpOrtu;
    }

    public void setNoHpOrtu(String noHpOrtu) {
        this.noHpOrtu = noHpOrtu;
    }

    public String getAlamatOrtu() {
        return alamatOrtu;
    }

    public void setAlamatOrtu(String alamatOrtu) {
        this.alamatOrtu = alamatOrtu;
    }

    public Integer getTahunMasuk() {
        return tahunMasuk;
    }

    public void setTahunMasuk(Integer tahunMasuk) {
        this.tahunMasuk = tahunMasuk;
    }

    public String getAsalSekolah() {
        return asalSekolah;
    }

    public void setAsalSekolah(String asalSekolah) {
        this.asalSekolah = asalSekolah;
    }
    
    public List<Attendance> getAttendances() {
        return attendances;
    }
    
    public void setAttendances(List<Attendance> attendances) {
        this.attendances = attendances;
    }
    
    public List<StudentAssessment> getAssessments() {
        return assessments;
    }
    
    public void setAssessments(List<StudentAssessment> assessments) {
        this.assessments = assessments;
    }
    
    public List<ExtracurricularActivity> getExtracurricularActivities() {
        return extracurricularActivities;
    }
    
    public void setExtracurricularActivities(List<ExtracurricularActivity> extracurricularActivities) {
        this.extracurricularActivities = extracurricularActivities;
    }
    
    // Convenience methods for ExtracurricularActivity mapping
    public String getFirstName() {
        if (namaLengkap == null) return null;
        String[] parts = namaLengkap.split(" ", 2);
        return parts[0];
    }
    
    public String getLastName() {
        if (namaLengkap == null) return null;
        String[] parts = namaLengkap.split(" ", 2);
        return parts.length > 1 ? parts[1] : "";
    }
    
    public String getStudentNumber() {
        return nis;
    }
    
    public String getEmail() {
        return user != null ? user.getEmail() : null;
    }
    
    public String getPhone() {
        return user != null ? user.getPhone() : null;
    }
    
    public String getClassName() {
        return classRoom != null ? classRoom.getName() : null;
    }

    // equals and hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Student student = (Student) o;
        return Objects.equals(nis, student.nis);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nis);
    }

    @Override
    public String toString() {
        return "Student{" +
                "id=" + id +
                ", nis='" + nis + '\'' +
                ", namaLengkap='" + namaLengkap + '\'' +
                ", status=" + status +
                '}';
    }
}
