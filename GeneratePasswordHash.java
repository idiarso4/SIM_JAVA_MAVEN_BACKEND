import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class GeneratePasswordHash {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        String adminPassword = "admin123";
        String teacherPassword = "teacher123";
        
        String adminHash = encoder.encode(adminPassword);
        String teacherHash = encoder.encode(teacherPassword);
        
        System.out.println("Admin password (admin123) hash: " + adminHash);
        System.out.println("Teacher password (teacher123) hash: " + teacherHash);
        
        // Test verification
        System.out.println("Admin verification: " + encoder.matches(adminPassword, adminHash));
        System.out.println("Teacher verification: " + encoder.matches(teacherPassword, teacherHash));
    }
}