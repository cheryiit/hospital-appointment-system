package com.hospital.config;

import com.hospital.entity.Role;
import com.hospital.entity.User;
import com.hospital.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DemoUserInitializer {

    @Bean
    CommandLineRunner createDemoUsers(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (!userRepository.existsByEmail("hasta@hastane.com")) {
                User patient = new User();
                patient.setFirstName("Demo");
                patient.setLastName("Hasta");
                patient.setEmail("hasta@hastane.com");
                patient.setPassword(passwordEncoder.encode("123456"));
                patient.setRole(Role.PATIENT);
                userRepository.save(patient);
            }

            if (!userRepository.existsByEmail("doktor@hastane.com")) {
                User doctor = new User();
                doctor.setFirstName("Demo");
                doctor.setLastName("Doktor");
                doctor.setEmail("doktor@hastane.com");
                doctor.setPassword(passwordEncoder.encode("123456"));
                doctor.setRole(Role.DOCTOR);
                userRepository.save(doctor);
            }
        };
    }
}
