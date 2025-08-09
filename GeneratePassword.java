import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class GeneratePassword {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        String admin123 = encoder.encode("admin123");
        String teacher123 = encoder.encode("teacher123");
        
        System.out.println("admin123 hash: " + admin123);
        System.out.println("teacher123 hash: " + teacher123);
    }
}