package com.school.sim.dto.response;

import com.school.sim.entity.Gender;
import com.school.sim.entity.Student;
import com.school.sim.entity.StudentStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Response DTO for student information
 */
public class StudentResponse {

    private Long id;
    private String nis;
    private String namaLengkap;
    private ClassRoomInfo classRoom;
    private String tempatLahir;
    private LocalDate tanggalLahir;
    private Gender jenisKelamin;
    private String agama;
    private String alamat;
    private String namaAyah;
    private String namaIbu;
    private String pekerjaanAyah;
    private String pekerjaanIbu;
    private String noHpOrtu;
    private String alamatOrtu;
    private Integer tahunMasuk;
    private String asalSekolah;
    private StudentStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private UserInfo user;

    // Constructors
    public StudentResponse() {}

    public StudentResponse(Student student) {
        this.id = student.getId();
        this.nis = student.getNis();
        this.namaLengkap = student.getNamaLengkap();
        this.tempatLahir = student.getTempatLahir();
        this.tanggalLahir = student.getTanggalLahir();
        this.jenisKelamin = student.getJenisKelamin();
        this.agama = student.getAgama();
        this.alamat = student.getAlamat();
        this.namaAyah = student.getNamaAyah();
        this.namaIbu = student.getNamaIbu();
        this.pekerjaanAyah = student.getPekerjaanAyah();
        this.pekerjaanIbu = student.getPekerjaanIbu();
        this.noHpOrtu = student.getNoHpOrtu();
        this.alamatOrtu = student.getAlamatOrtu();
        this.tahunMasuk = student.getTahunMasuk();
        this.asalSekolah = student.getAsalSekolah();
        this.status = student.getStatus();
        this.createdAt = student.getCreatedAt();
        this.updatedAt = student.getUpdatedAt();

        if (student.getClassRoom() != null) {
            this.classRoom = new ClassRoomInfo(student.getClassRoom());
        }

        if (student.getUser() != null) {
            this.user = new UserInfo(student.getUser());
        }
    }

    // Static factory method
    public static StudentResponse from(Student student) {
        return new StudentResponse(student);
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

    public ClassRoomInfo getClassRoom() {
        return classRoom;
    }

    public void setClassRoom(ClassRoomInfo classRoom) {
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

    public UserInfo getUser() {
        return user;
    }

    public void setUser(UserInfo user) {
        this.user = user;
    }

    /**
     * Inner class for class room information
     */
    public static class ClassRoomInfo {
        private Long id;
        private String name;
        private Integer grade;
        private String academicYear;
        private MajorInfo major;

        public ClassRoomInfo() {}

        public ClassRoomInfo(com.school.sim.entity.ClassRoom classRoom) {
            this.id = classRoom.getId();
            this.name = classRoom.getName();
            this.grade = classRoom.getGrade();
            this.academicYear = classRoom.getAcademicYear();
            
            if (classRoom.getMajor() != null) {
                this.major = new MajorInfo(classRoom.getMajor());
            }
        }

        // Getters and setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public Integer getGrade() { return grade; }
        public void setGrade(Integer grade) { this.grade = grade; }
        public String getAcademicYear() { return academicYear; }
        public void setAcademicYear(String academicYear) { this.academicYear = academicYear; }
        public MajorInfo getMajor() { return major; }
        public void setMajor(MajorInfo major) { this.major = major; }
    }

    /**
     * Inner class for major information
     */
    public static class MajorInfo {
        private Long id;
        private String name;
        private String code;
        private DepartmentInfo department;

        public MajorInfo() {}

        public MajorInfo(com.school.sim.entity.Major major) {
            this.id = major.getId();
            this.name = major.getName();
            this.code = major.getCode();
            
            if (major.getDepartment() != null) {
                this.department = new DepartmentInfo(major.getDepartment());
            }
        }

        // Getters and setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
        public DepartmentInfo getDepartment() { return department; }
        public void setDepartment(DepartmentInfo department) { this.department = department; }
    }

    /**
     * Inner class for department information
     */
    public static class DepartmentInfo {
        private Long id;
        private String name;
        private String code;

        public DepartmentInfo() {}

        public DepartmentInfo(com.school.sim.entity.Department department) {
            this.id = department.getId();
            this.name = department.getName();
            this.code = department.getCode();
        }

        // Getters and setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
    }

    /**
     * Inner class for user information
     */
    public static class UserInfo {
        private Long id;
        private String name;
        private String email;
        private boolean active;

        public UserInfo() {}

        public UserInfo(com.school.sim.entity.User user) {
            this.id = user.getId();
            this.name = user.getFirstName() + " " + user.getLastName();
            this.email = user.getEmail();
            this.active = user.getIsActive();
        }

        // Getters and setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public boolean isActive() { return active; }
        public void setActive(boolean active) { this.active = active; }
    }
}
