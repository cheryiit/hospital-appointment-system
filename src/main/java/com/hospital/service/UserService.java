package com.hospital.service;

import com.hospital.entity.Role;
import com.hospital.entity.User;
import com.hospital.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public User registerUser(User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Bu email adresi zaten kayıtlı!");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    public User authenticateUser(String email, String password) {
        System.out.println("Authenticating: " + email);

        Optional<User> userOpt = userRepository.findByEmail(email);

        if (userOpt.isEmpty()) {
            System.out.println("User not found: " + email);
            throw new RuntimeException("Email veya şifre hatalı!");
        }

        User user = userOpt.get();
        System.out.println("User found: " + user.getEmail() + " - Role: " + user.getRole());

        if (passwordEncoder.matches(password, user.getPassword())) {
            System.out.println("Password correct for: " + email);
            return user;
        } else {
            System.out.println("Password incorrect for: " + email);
            throw new RuntimeException("Email veya şifre hatalı!");
        }
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public List<User> getAllDoctors() {
        return userRepository.findByRole(Role.DOCTOR);
    }

    public List<User> getAllPatients() {
        return userRepository.findByRole(Role.PATIENT);
    }

    public List<User> searchDoctors(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return getAllDoctors();
        }
        return userRepository.findByRoleAndSearch(Role.DOCTOR, searchTerm.trim());
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public long getTotalPatients() {
        return userRepository.countByRole(Role.PATIENT);
    }

    public long getTotalDoctors() {
        return userRepository.countByRole(Role.DOCTOR);
    }
}