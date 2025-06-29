package org.example.proyecto_final_calidad.security;

import org.example.proyecto_final_calidad.model.User;
import org.example.proyecto_final_calidad.repositorio.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of UserDetailsService for loading user-specific data.
 * It loads user details from the database using the UserRepository.
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    /**
     * Load user by username.
     * This method is used by Spring Security to load user details during authentication.
     *
     * @param username the username to load
     * @return the UserDetails object
     * @throws UsernameNotFoundException if the user is not found
     */
    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        // Our User class already implements UserDetails, so we can return it directly
        return user;
    }
}