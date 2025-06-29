package org.example.proyecto_final_calidad.repositorio;

import org.example.proyecto_final_calidad.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for User entity.
 * Provides methods to interact with the users table in the database.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    /**
     * Find a user by username.
     * 
     * @param username the username to search for
     * @return an Optional containing the user if found, or empty if not found
     */
    Optional<User> findByUsername(String username);
    
    /**
     * Check if a user with the given username exists.
     * 
     * @param username the username to check
     * @return true if a user with the username exists, false otherwise
     */
    boolean existsByUsername(String username);
    
    /**
     * Check if a user with the given email exists.
     * 
     * @param email the email to check
     * @return true if a user with the email exists, false otherwise
     */
    boolean existsByEmail(String email);
}