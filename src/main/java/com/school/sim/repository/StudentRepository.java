package com.school.sim.repository;

import com.school.sim.entity.Student;
import com.school.sim.entity.StudentStatus;
import com.school.sim.entity.Gender;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Student entity operations
 */
@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {

       /**
        * Find student by NIS
        */
       Optional<Student> findByNis(String nis);

       /**
        * Find students by status
        */
       List<Student> findByStatus(StudentStatus status);

       /**
        * Find students by status with pagination
        */
       Page<Student> findByStatus(StudentStatus status, Pageable pageable);

       /**
        * Find students by class room ID
        */
       List<Student> findByClassRoomId(Long classRoomId);

       /**
        * Find students by class room ID with pagination
        */
       Page<Student> findByClassRoomId(Long classRoomId, Pageable pageable);

       /**
        * Find students by name containing (case insensitive)
        */
       Page<Student> findByNamaLengkapContainingIgnoreCase(String name, Pageable pageable);

       /**
        * Find students by NIS containing
        */
       Page<Student> findByNisContaining(String nis, Pageable pageable);

       /**
        * Find students by class room and status
        */
       List<Student> findByClassRoomIdAndStatus(Long classRoomId, StudentStatus status);

       /**
        * Find students by class room and status with pagination
        */
       Page<Student> findByClassRoomIdAndStatus(Long classRoomId, StudentStatus status, Pageable pageable);

       /**
        * Find students by tahun masuk (year of entry)
        */
       List<Student> findByTahunMasuk(Integer tahunMasuk);

       /**
        * Find students by tahun masuk with pagination
        */
       Page<Student> findByTahunMasuk(Integer tahunMasuk, Pageable pageable);

       /**
        * Check if NIS exists
        */
       boolean existsByNis(String nis);

       /**
        * Count students by status
        */
       long countByStatus(StudentStatus status);

       /**
        * Count students by class room
        */
       long countByClassRoomId(Long classRoomId);

       /**
        * Count students by class room entity
        */
       long countByClassRoom(com.school.sim.entity.ClassRoom classRoom);

       /**
        * Count students by grade (through class room)
        */
       @Query("SELECT COUNT(s) FROM Student s WHERE s.classRoom.grade = :grade")
       long countByClassRoomGrade(@Param("grade") Integer grade);

       /**
        * Search students with multiple criteria
        */
       @Query("SELECT s FROM Student s WHERE " +
                     "(:namaLengkap IS NULL OR LOWER(s.namaLengkap) LIKE LOWER(CONCAT('%', :namaLengkap, '%'))) AND " +
                     "(:nis IS NULL OR LOWER(s.nis) LIKE LOWER(CONCAT('%', :nis, '%'))) AND " +
                     "(:status IS NULL OR s.status = :status) AND " +
                     "(:classRoomId IS NULL OR s.classRoom.id = :classRoomId)")
       List<Student> searchStudents(@Param("namaLengkap") String namaLengkap,
                     @Param("nis") String nis,
                     @Param("status") StudentStatus status,
                     @Param("classRoomId") Long classRoomId);

       /**
        * Count active students in a class room
        */
       long countByClassRoomIdAndStatus(Long classRoomId, StudentStatus status);

       /**
        * Find students by major (through class room)
        */
       @Query("SELECT s FROM Student s WHERE s.classRoom.major.id = :majorId")
       List<Student> findByMajorId(@Param("majorId") Long majorId);

       /**
        * Find students by major with pagination
        */
       @Query("SELECT s FROM Student s WHERE s.classRoom.major.id = :majorId")
       Page<Student> findByMajorId(@Param("majorId") Long majorId, Pageable pageable);

       /**
        * Find students by department (through class room and major)
        */
       @Query("SELECT s FROM Student s WHERE s.classRoom.major.department.id = :departmentId")
       List<Student> findByDepartmentId(@Param("departmentId") Long departmentId);

       /**
        * Find students by grade (through class room)
        */
       @Query("SELECT s FROM Student s WHERE s.classRoom.grade = :grade")
       List<Student> findByGrade(@Param("grade") Integer grade);

       /**
        * Find students by grade with pagination
        */
       @Query("SELECT s FROM Student s WHERE s.classRoom.grade = :grade")
       Page<Student> findByGrade(@Param("grade") Integer grade, Pageable pageable);

       /**
        * Find students without class room assignment
        */
       List<Student> findByClassRoomIsNull();

       /**
        * Find students with user account
        */
       @Query("SELECT s FROM Student s WHERE s.user IS NOT NULL")
       List<Student> findStudentsWithUserAccount();

       /**
        * Find students without user account
        */
       @Query("SELECT s FROM Student s WHERE s.user IS NULL")
       List<Student> findStudentsWithoutUserAccount();

       /**
        * Search students by multiple criteria
        */
       @Query("SELECT s FROM Student s WHERE " +
                     "(:name IS NULL OR LOWER(s.namaLengkap) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
                     "(:nis IS NULL OR s.nis LIKE CONCAT('%', :nis, '%')) AND " +
                     "(:classRoomId IS NULL OR s.classRoom.id = :classRoomId) AND " +
                     "(:status IS NULL OR s.status = :status) AND " +
                     "(:tahunMasuk IS NULL OR s.tahunMasuk = :tahunMasuk)")
       Page<Student> searchStudents(@Param("name") String name,
                     @Param("nis") String nis,
                     @Param("classRoomId") Long classRoomId,
                     @Param("status") StudentStatus status,
                     @Param("tahunMasuk") Integer tahunMasuk,
                     Pageable pageable);

       /**
        * Find students by birth year
        */
       @Query("SELECT s FROM Student s WHERE YEAR(s.tanggalLahir) = :birthYear")
       List<Student> findByBirthYear(@Param("birthYear") Integer birthYear);

       /**
        * Find students by age range
        */
       @Query("SELECT s FROM Student s WHERE YEAR(CURRENT_DATE) - YEAR(s.tanggalLahir) BETWEEN :minAge AND :maxAge")
       List<Student> findByAgeRange(@Param("minAge") Integer minAge, @Param("maxAge") Integer maxAge);

       /**
        * Find students by gender
        */
       List<Student> findByJenisKelamin(Gender jenisKelamin);

       /**
        * Count students by gender
        */
       long countByJenisKelamin(Gender jenisKelamin);

       /**
        * Find students by religion
        */
       List<Student> findByAgama(String agama);

       /**
        * Find students by parent phone number
        */
       List<Student> findByNoHpOrtu(String noHpOrtu);

       /**
        * Find students by origin school
        */
       List<Student> findByAsalSekolah(String asalSekolah);

       /**
        * Find students with attendance records
        */
       @Query("SELECT DISTINCT s FROM Student s JOIN s.attendances a")
       List<Student> findStudentsWithAttendanceRecords();

       /**
        * Find students without attendance records
        */
       @Query("SELECT s FROM Student s WHERE s.attendances IS EMPTY")
       List<Student> findStudentsWithoutAttendanceRecords();

       /**
        * Find students with assessment records
        */
       @Query("SELECT DISTINCT s FROM Student s JOIN s.assessments a")
       List<Student> findStudentsWithAssessmentRecords();

       /**
        * Find students without assessment records
        */
       @Query("SELECT s FROM Student s WHERE s.assessments IS EMPTY")
       List<Student> findStudentsWithoutAssessmentRecords();

       /**
        * Find students by class room name
        */
       @Query("SELECT s FROM Student s WHERE LOWER(s.classRoom.name) LIKE LOWER(CONCAT('%', :className, '%'))")
       List<Student> findByClassRoomNameContainingIgnoreCase(@Param("className") String className);

       /**
        * Find students by major name
        */
       @Query("SELECT s FROM Student s WHERE LOWER(s.classRoom.major.name) LIKE LOWER(CONCAT('%', :majorName, '%'))")
       List<Student> findByMajorNameContainingIgnoreCase(@Param("majorName") String majorName);

       /**
        * Find students by department name
        */
       @Query("SELECT s FROM Student s WHERE LOWER(s.classRoom.major.department.name) LIKE LOWER(CONCAT('%', :departmentName, '%'))")
       List<Student> findByDepartmentNameContainingIgnoreCase(@Param("departmentName") String departmentName);

       /**
        * Get student statistics by status
        */
       @Query("SELECT s.status, COUNT(s) FROM Student s GROUP BY s.status")
       List<Object[]> getStudentStatisticsByStatus();

       /**
        * Get student statistics by grade
        */
       @Query("SELECT s.classRoom.grade, COUNT(s) FROM Student s WHERE s.classRoom IS NOT NULL GROUP BY s.classRoom.grade ORDER BY s.classRoom.grade")
       List<Object[]> getStudentStatisticsByGrade();

       /**
        * Get student statistics by major
        */
       @Query("SELECT s.classRoom.major.name, COUNT(s) FROM Student s WHERE s.classRoom IS NOT NULL GROUP BY s.classRoom.major.name ORDER BY s.classRoom.major.name")
       List<Object[]> getStudentStatisticsByMajor();

       /**
        * Find students enrolled in specific academic year
        */
       @Query("SELECT s FROM Student s WHERE s.classRoom.academicYear = :academicYear")
       List<Student> findByAcademicYear(@Param("academicYear") String academicYear);

       /**
        * Find students enrolled in specific academic year with pagination
        */
       @Query("SELECT s FROM Student s WHERE s.classRoom.academicYear = :academicYear")
       Page<Student> findByAcademicYear(@Param("academicYear") String academicYear, Pageable pageable);

       /**
        * Advanced search with multiple criteria including parent information
        */
       @Query("SELECT s FROM Student s WHERE " +
                     "(:name IS NULL OR LOWER(s.namaLengkap) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
                     "(:nis IS NULL OR s.nis LIKE CONCAT('%', :nis, '%')) AND " +
                     "(:classRoomId IS NULL OR s.classRoom.id = :classRoomId) AND " +
                     "(:status IS NULL OR s.status = :status) AND " +
                     "(:tahunMasuk IS NULL OR s.tahunMasuk = :tahunMasuk) AND " +
                     "(:agama IS NULL OR s.agama = :agama) AND " +
                     "(:asalSekolah IS NULL OR LOWER(s.asalSekolah) LIKE LOWER(CONCAT('%', :asalSekolah, '%'))) AND " +
                     "(:parentName IS NULL OR LOWER(s.namaAyah) LIKE LOWER(CONCAT('%', :parentName, '%')) OR LOWER(s.namaIbu) LIKE LOWER(CONCAT('%', :parentName, '%')))")
       Page<Student> advancedSearchStudents(@Param("name") String name,
                     @Param("nis") String nis,
                     @Param("classRoomId") Long classRoomId,
                     @Param("status") StudentStatus status,
                     @Param("tahunMasuk") Integer tahunMasuk,
                     @Param("agama") String agama,
                     @Param("asalSekolah") String asalSekolah,
                     @Param("parentName") String parentName,
                     Pageable pageable);
}
