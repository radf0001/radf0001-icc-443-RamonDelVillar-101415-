package org.example.proyecto_final_calidad.security;

import org.example.proyecto_final_calidad.model.Role;
import org.example.proyecto_final_calidad.model.User;
import org.example.proyecto_final_calidad.repositorio.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Component to initialize the first admin user when the application starts.
 * This ensures there's at least one administrator account available.
 */
@Component
public class InitialUserSetup implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(InitialUserSetup.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Run method executed on application startup.
     * Creates an admin user if no users exist in the database.
     *
     * @param args command line arguments
     */
    @Override
    public void run(String... args) {
        // Check if any users exist
        if (userRepository.count() == 0) {
            logger.info("No users found, creating initial admin user");

            // Create admin user
            User adminUser = new User(
                    "admin",
                    passwordEncoder.encode("admin123"),
                    "admin@example.com",
                    Role.ADMINISTRATOR
            );

            userRepository.save(adminUser);
            logger.info("Initial admin user created successfully");
        }
    }
}