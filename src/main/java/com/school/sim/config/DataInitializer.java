package com.school.sim.config;

import com.school.sim.entity.*;
import com.school.sim.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * Data Initializer to populate database with initial data
 */
//@Component
public class DataInitializer {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void initializeData() {
        logger.info("Starting data initialization...");

        try {
            // Initialize roles
            initializeRoles();
            
            // Initialize permissions
            initializePermissions();
            
            // Initialize users
            initializeUsers();
            
            // Initialize students
            initializeStudents();
            
            logger.info("Data initialization completed successfully");
        } catch (Exception e) {
            logger.error("Error during data initialization", e);
            throw e;
        }
    }

    private void initializeRoles() {
        logger.info("Initializing roles...");
        
        if (roleRepository.count() == 0) {
            List<Role> roles = Arrays.asList(
                new Role("ADMIN", "System Administrator", true),
                new Role("TEACHER", "Teacher", true),
                new Role("STUDENT", "Student", true)
            );
            
            roleRepository.saveAll(roles);
            logger.info("Created {} roles", roles.size());
        } else {
            logger.info("Roles already exist, skipping initialization");
        }
    }

    private void initializePermissions() {
        logger.info("Initializing permissions...");
        
        if (permissionRepository.count() == 0) {
            List<Permission> permissions = Arrays.asList(
                new Permission("VIEW_STUDENTS", "View student information"),
                new Permission("MANAGE_STUDENTS", "Create, update, delete students"),
                new Permission("VIEW_USERS", "View user information"),
                new Permission("MANAGE_USERS", "Create, update, delete users"),
                new Permission("VIEW_GRADES", "View grades and assessments"),
                new Permission("MANAGE_GRADES", "Create, update, delete grades"),
                new Permission("VIEW_REPORTS", "View reports"),
                new Permission("GENERATE_REPORTS", "Generate and export reports"),
                new Permission("MANAGE_CLASSES", "Manage class schedules and assignments"),
                new Permission("VIEW_ATTENDANCE", "View attendance records"),
                new Permission("MANAGE_ATTENDANCE", "Manage attendance records")
            );
            
            permissionRepository.saveAll(permissions);
            logger.info("Created {} permissions", permissions.size());
            
            // Assign permissions to roles
            assignPermissionsToRoles();
        } else {
            logger.info("Permissions already exist, skipping initialization");
        }
    }

    private void assignPermissionsToRoles() {
        logger.info("Assigning permissions to roles...");
        
        Role adminRole = roleRepository.findByName("ADMIN").orElse(null);
        Role teacherRole = roleRepository.findByName("TEACHER").orElse(null);
        Role studentRole = roleRepository.findByName("STUDENT").orElse(null);
        
        if (adminRole != null) {
            // Admin gets all permissions
            List<Permission> allPermissions = permissionRepository.findAll();
            adminRole.getPermissions().addAll(allPermissions);
            roleRepository.save(adminRole);
            logger.info("Assigned {} permissions to ADMIN role", allPermissions.size());
        }
        
        if (teacherRole != null) {
            // Teacher gets limited permissions
            List<String> teacherPermissionNames = Arrays.asList(
                "VIEW_STUDENTS", "VIEW_GRADES", "MANAGE_GRADES", 
                "VIEW_ATTENDANCE", "MANAGE_ATTENDANCE", "VIEW_REPORTS"
            );
            for (String permissionName : teacherPermissionNames) {
                Permission permission = permissionRepository.findByName(permissionName).orElse(null);
                if (permission != null) {
                    teacherRole.getPermissions().add(permission);
                }
            }
            roleRepository.save(teacherRole);
            logger.info("Assigned {} permissions to TEACHER role", teacherRole.getPermissions().size());
        }
        
        if (studentRole != null) {
            // Student gets very limited permissions
            Permission viewGradesPermission = permissionRepository.findByName("VIEW_GRADES").orElse(null);
            if (viewGradesPermission != null) {
                studentRole.getPermissions().add(viewGradesPermission);
            }
            roleRepository.save(studentRole);
            logger.info("Assigned {} permissions to STUDENT role", studentRole.getPermissions().size());
        }
    }

    private void initializeUsers() {
        logger.info("Initializing users...");
        
        if (userRepository.count() == 0) {
            Role adminRole = roleRepository.findByName("ADMIN").orElse(null);
            Role teacherRole = roleRepository.findByName("TEACHER").orElse(null);
            
            // Create admin user
            if (adminRole != null) {
                User adminUser = User.builder()
                    .username("admin")
                    .email("admin@sim.edu")
                    .firstName("System")
                    .lastName("Administrator")
                    .password(passwordEncoder.encode("admin123"))
                    .userType(UserType.ADMIN)
                    .isActive(true)
                    .emailVerifiedAt(LocalDateTime.now())
                    .build();
                
                adminUser.getRoles().add(adminRole);
                userRepository.save(adminUser);
                logger.info("Created admin user");
            }
            
            // Create teacher user
            if (teacherRole != null) {
                User teacherUser = User.builder()
                    .username("teacher")
                    .email("teacher@sim.edu")
                    .firstName("Test")
                    .lastName("Teacher")
                    .password(passwordEncoder.encode("teacher123"))
                    .userType(UserType.TEACHER)
                    .nip("T001")
                    .isActive(true)
                    .emailVerifiedAt(LocalDateTime.now())
                    .build();
                
                teacherUser.getRoles().add(teacherRole);
                userRepository.save(teacherUser);
                logger.info("Created teacher user");
            }
        } else {
            logger.info("Users already exist, skipping initialization");
        }
    }

    private void initializeStudents() {
        logger.info("Initializing students...");
        
        if (studentRepository.count() == 0) {
            List<Student> students = Arrays.asList(
                Student.builder()
                    .nis("2024001")
                    .namaLengkap("Ahmad Rizki Pratama")
                    .tempatLahir("Jakarta")
                    .tanggalLahir(LocalDate.of(2006, 3, 15))
                    .jenisKelamin(Gender.LAKI_LAKI)
                    .agama("Islam")
                    .alamat("Jl. Merdeka No. 123, Jakarta")
                    .namaAyah("Budi Pratama")
                    .namaIbu("Siti Nurhaliza")
                    .pekerjaanAyah("Pegawai Swasta")
                    .pekerjaanIbu("Ibu Rumah Tangga")
                    .noHpOrtu("081234567890")
                    .alamatOrtu("Jl. Merdeka No. 123, Jakarta")
                    .tahunMasuk(2024)
                    .asalSekolah("SMP Negeri 1 Jakarta")
                    .status(StudentStatus.ACTIVE)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build(),
                Student.builder()
                    .nis("2024002")
                    .namaLengkap("Sari Dewi Lestari")
                    .tempatLahir("Bandung")
                    .tanggalLahir(LocalDate.of(2006, 7, 22))
                    .jenisKelamin(Gender.PEREMPUAN)
                    .agama("Islam")
                    .alamat("Jl. Sudirman No. 456, Bandung")
                    .namaAyah("Andi Lestari")
                    .namaIbu("Dewi Sartika")
                    .pekerjaanAyah("Guru")
                    .pekerjaanIbu("Pegawai Bank")
                    .noHpOrtu("081234567891")
                    .alamatOrtu("Jl. Sudirman No. 456, Bandung")
                    .tahunMasuk(2024)
                    .asalSekolah("SMP Negeri 2 Bandung")
                    .status(StudentStatus.ACTIVE)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build(),
                Student.builder()
                    .nis("2024003")
                    .namaLengkap("Muhammad Fajar Sidiq")
                    .tempatLahir("Surabaya")
                    .tanggalLahir(LocalDate.of(2006, 1, 10))
                    .jenisKelamin(Gender.LAKI_LAKI)
                    .agama("Islam")
                    .alamat("Jl. Pemuda No. 789, Surabaya")
                    .namaAyah("Hasan Sidiq")
                    .namaIbu("Fatimah Zahra")
                    .pekerjaanAyah("Wiraswasta")
                    .pekerjaanIbu("Guru")
                    .noHpOrtu("081234567892")
                    .alamatOrtu("Jl. Pemuda No. 789, Surabaya")
                    .tahunMasuk(2024)
                    .asalSekolah("SMP Negeri 3 Surabaya")
                    .status(StudentStatus.ACTIVE)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build(),
                Student.builder()
                    .nis("2024004")
                    .namaLengkap("Indira Putri Maharani")
                    .tempatLahir("Yogyakarta")
                    .tanggalLahir(LocalDate.of(2006, 5, 18))
                    .jenisKelamin(Gender.PEREMPUAN)
                    .agama("Hindu")
                    .alamat("Jl. Malioboro No. 321, Yogyakarta")
                    .namaAyah("I Made Maharani")
                    .namaIbu("Ni Kadek Sari")
                    .pekerjaanAyah("Pegawai Negeri")
                    .pekerjaanIbu("Dokter")
                    .noHpOrtu("081234567893")
                    .alamatOrtu("Jl. Malioboro No. 321, Yogyakarta")
                    .tahunMasuk(2024)
                    .asalSekolah("SMP Negeri 1 Yogyakarta")
                    .status(StudentStatus.ACTIVE)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build(),
                Student.builder()
                    .nis("2024005")
                    .namaLengkap("Kevin Alexander Wijaya")
                    .tempatLahir("Medan")
                    .tanggalLahir(LocalDate.of(2006, 9, 3))
                    .jenisKelamin(Gender.LAKI_LAKI)
                    .agama("Kristen")
                    .alamat("Jl. Asia No. 654, Medan")
                    .namaAyah("Alexander Wijaya")
                    .namaIbu("Maria Susanti")
                    .pekerjaanAyah("Pengusaha")
                    .pekerjaanIbu("Akuntan")
                    .noHpOrtu("081234567894")
                    .alamatOrtu("Jl. Asia No. 654, Medan")
                    .tahunMasuk(2024)
                    .asalSekolah("SMP Swasta Medan")
                    .status(StudentStatus.ACTIVE)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build(),
                Student.builder()
                    .nis("2023001")
                    .namaLengkap("Rina Sari Dewi")
                    .tempatLahir("Semarang")
                    .tanggalLahir(LocalDate.of(2005, 11, 12))
                    .jenisKelamin(Gender.PEREMPUAN)
                    .agama("Islam")
                    .alamat("Jl. Pandanaran No. 111, Semarang")
                    .namaAyah("Bambang Dewi")
                    .namaIbu("Sari Wulandari")
                    .pekerjaanAyah("Pegawai Swasta")
                    .pekerjaanIbu("Guru")
                    .noHpOrtu("081234567895")
                    .alamatOrtu("Jl. Pandanaran No. 111, Semarang")
                    .tahunMasuk(2023)
                    .asalSekolah("SMP Negeri 5 Semarang")
                    .status(StudentStatus.ACTIVE)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build(),
                Student.builder()
                    .nis("2023002")
                    .namaLengkap("Dimas Arya Pratama")
                    .tempatLahir("Makassar")
                    .tanggalLahir(LocalDate.of(2005, 4, 25))
                    .jenisKelamin(Gender.LAKI_LAKI)
                    .agama("Islam")
                    .alamat("Jl. Veteran No. 222, Makassar")
                    .namaAyah("Arya Gunawan")
                    .namaIbu("Lestari Pratama")
                    .pekerjaanAyah("TNI")
                    .pekerjaanIbu("Perawat")
                    .noHpOrtu("081234567896")
                    .alamatOrtu("Jl. Veteran No. 222, Makassar")
                    .tahunMasuk(2023)
                    .asalSekolah("SMP Negeri 2 Makassar")
                    .status(StudentStatus.ACTIVE)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build(),
                Student.builder()
                    .nis("2023003")
                    .namaLengkap("Putri Ayu Lestari")
                    .tempatLahir("Palembang")
                    .tanggalLahir(LocalDate.of(2005, 8, 14))
                    .jenisKelamin(Gender.PEREMPUAN)
                    .agama("Islam")
                    .alamat("Jl. Sudirman No. 333, Palembang")
                    .namaAyah("Lestari Budi")
                    .namaIbu("Ayu Sari")
                    .pekerjaanAyah("Polisi")
                    .pekerjaanIbu("Bidan")
                    .noHpOrtu("081234567897")
                    .alamatOrtu("Jl. Sudirman No. 333, Palembang")
                    .tahunMasuk(2023)
                    .asalSekolah("SMP Negeri 1 Palembang")
                    .status(StudentStatus.ACTIVE)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build(),
                Student.builder()
                    .nis("2022001")
                    .namaLengkap("Bayu Adi Nugroho")
                    .tempatLahir("Solo")
                    .tanggalLahir(LocalDate.of(2004, 12, 8))
                    .jenisKelamin(Gender.LAKI_LAKI)
                    .agama("Islam")
                    .alamat("Jl. Slamet Riyadi No. 444, Solo")
                    .namaAyah("Adi Nugroho")
                    .namaIbu("Siti Bayu")
                    .pekerjaanAyah("Pegawai Bank")
                    .pekerjaanIbu("Guru")
                    .noHpOrtu("081234567898")
                    .alamatOrtu("Jl. Slamet Riyadi No. 444, Solo")
                    .tahunMasuk(2022)
                    .asalSekolah("SMP Negeri 3 Solo")
                    .status(StudentStatus.ACTIVE)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build(),
                Student.builder()
                    .nis("2022002")
                    .namaLengkap("Citra Dewi Anggraini")
                    .tempatLahir("Malang")
                    .tanggalLahir(LocalDate.of(2004, 6, 30))
                    .jenisKelamin(Gender.PEREMPUAN)
                    .agama("Kristen")
                    .alamat("Jl. Ijen No. 555, Malang")
                    .namaAyah("Dewi Santoso")
                    .namaIbu("Anggraini Putri")
                    .pekerjaanAyah("Dosen")
                    .pekerjaanIbu("Dokter")
                    .noHpOrtu("081234567899")
                    .alamatOrtu("Jl. Ijen No. 555, Malang")
                    .tahunMasuk(2022)
                    .asalSekolah("SMP Negeri 1 Malang")
                    .status(StudentStatus.GRADUATED)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build()
            );
            
            studentRepository.saveAll(students);
            logger.info("Created {} students", students.size());
        } else {
            logger.info("Students already exist, skipping initialization");
        }
    }
}