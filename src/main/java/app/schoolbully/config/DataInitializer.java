package app.schoolbully.config;

import app.schoolbully.model.entity.User;
import app.schoolbully.model.enums.Role;
import app.schoolbully.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        // Create a test admin user if it doesn't exist
        if (userRepository.findByEmail("admin@school.com").isEmpty()) {
            User admin = new User();
            admin.setFirstName("Admin");
            admin.setLastName("User");
            admin.setEmail("admin@school.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole(Role.Admin);
            admin.setActive(true);
            userRepository.save(admin);
            log.info("Created default admin user: admin@school.com / admin123");
        }

        // Create a test student user if it doesn't exist
        if (userRepository.findByEmail("student@school.com").isEmpty()) {
            User student = new User();
            student.setFirstName("Test");
            student.setLastName("Student");
            student.setEmail("student@school.com");
            student.setPassword(passwordEncoder.encode("student123"));
            student.setRole(Role.Student);
            student.setActive(true);
            userRepository.save(student);
            log.info("Created default student user: student@school.com / student123");
        }

        // Create a test teacher user if it doesn't exist
        if (userRepository.findByEmail("teacher@school.com").isEmpty()) {
            User teacher = new User();
            teacher.setFirstName("Test");
            teacher.setLastName("Teacher");
            teacher.setEmail("teacher@school.com");
            teacher.setPassword(passwordEncoder.encode("teacher123"));
            teacher.setRole(Role.Teacher);
            teacher.setActive(true);
            userRepository.save(teacher);
            log.info("Created default teacher user: teacher@school.com / teacher123");
        }
    }
}

