package com.school.sim.dto.request;

import com.school.sim.entity.Gender;
import com.school.sim.entity.StudentStatus;
import javax.validation.constraints.Size;
import javax.validation.constraints.Past;
import javax.validation.constraints.Pattern;

import java.time.LocalDate;

/**
 * Request DTO for updating an existing student
 */
public class UpdateStudentRequest {

    @Size(max = 100, message = "Name must not exceed 100 characters")
    private String namaLengkap;

    private Long classRoomId;

    @Size(max = 50, message = "Place of birth must not exceed 50 characters")
    private String tempatLahir;

    @Past(message = "Birth date must be in the past")
    private LocalDate tanggalLahir;

    private Gender jenisKelamin;

    @Size(max = 20, message = "Religion must not exceed 20 characters")
    private String agama;

    @Size(max = 500, message = "Address must not exceed 500 characters")
    private String alamat;

    @Size(max = 100, message = "Father's name must not exceed 100 characters")
    private String namaAyah;

    @Size(max = 100, message = "Mother's name must not exceed 100 characters")
    private String namaIbu;

    @Size(max = 50, message = "Father's occupation must not exceed 50 characters")
    private String pekerjaanAyah;

    @Size(max = 50, message = "Mother's occupation must not exceed 50 characters")
    private String pekerjaanIbu;

    @Size(max = 20, message = "Parent phone number must not exceed 20 characters")
    @Pattern(regexp = "^[0-9+\\-\\s]*$", message = "Invalid phone number format")
    private String noHpOrtu;

    @Size(max = 500, message = "Parent address must not exceed 500 characters")
    private String alamatOrtu;

    private Integer tahunMasuk;

    @Size(max = 100, message = "Origin school must not exceed 100 characters")
    private String asalSekolah;

    private StudentStatus status;

    // Constructors
    public UpdateStudentRequest() {}

    // Getters and Setters
    public String getNamaLengkap() {
        return namaLengkap;
    }

    public void setNamaLengkap(String namaLengkap) {
        this.namaLengkap = namaLengkap;
    }

    public Long getClassRoomId() {
        return classRoomId;
    }

    public void setClassRoomId(Long classRoomId) {
        this.classRoomId = classRoomId;
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
}
