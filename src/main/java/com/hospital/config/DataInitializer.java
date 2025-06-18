package com.hospital.config;

import com.hospital.entity.Role;
import com.hospital.entity.User;
import com.hospital.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Demo hasta oluştur
        if (userRepository.findByEmail("hasta@hastane.com").isEmpty()) {
            User patient = new User();
            patient.setFirstName("Demo");
            patient.setLastName("Hasta");
            patient.setEmail("hasta@hastane.com");
            patient.setPassword(passwordEncoder.encode("123456"));
            patient.setPhone("05559876543");
            patient.setRole(Role.PATIENT);

            userRepository.save(patient);
            System.out.println("Demo hasta oluşturuldu - Email: hasta@hastane.com, Şifre: 123456");
        }

        // Demo doktor oluştur
        if (userRepository.findByEmail("doktor@hastane.com").isEmpty()) {
            User doctor = new User();
            doctor.setFirstName("Dr. Mehmet");
            doctor.setLastName("Özkan");
            doctor.setEmail("doktor@hastane.com");
            doctor.setPassword(passwordEncoder.encode("123456"));
            doctor.setPhone("05551234567");
            doctor.setRole(Role.DOCTOR);
            doctor.setSpecialization("Kardiyoloji");
            doctor.setLicenseNumber("DOC001");

            userRepository.save(doctor);
            System.out.println("Demo doktor oluşturuldu - Email: doktor@hastane.com, Şifre: 123456");
        }

        // Demo doktor 2
        if (userRepository.findByEmail("doktor2@hastane.com").isEmpty()) {
            User doctor2 = new User();
            doctor2.setFirstName("Dr. Ayşe");
            doctor2.setLastName("Demir");
            doctor2.setEmail("doktor2@hastane.com");
            doctor2.setPassword(passwordEncoder.encode("123456"));
            doctor2.setPhone("05551234568");
            doctor2.setRole(Role.DOCTOR);
            doctor2.setSpecialization("Nöroloji");
            doctor2.setLicenseNumber("DOC002");

            userRepository.save(doctor2);
            System.out.println("Demo doktor 2 oluşturuldu - Email: doktor2@hastane.com, Şifre: 123456");
        }
    }
}