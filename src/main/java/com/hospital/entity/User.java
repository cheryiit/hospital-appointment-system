package com.hospital.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Ad alanı boş olamaz")
    @Size(min = 2, max = 50, message = "Ad 2-50 karakter arasında olmalıdır")
    @Column(nullable = false)
    private String firstName;

    @NotBlank(message = "Soyad alanı boş olamaz")
    @Size(min = 2, max = 50, message = "Soyad 2-50 karakter arasında olmalıdır")
    @Column(nullable = false)
    private String lastName;

    @Email(message = "Geçerli bir email adresi giriniz")
    @NotBlank(message = "Email alanı boş olamaz")
    @Column(nullable = false, unique = true)
    private String email;

    @NotBlank(message = "Şifre alanı boş olamaz")
    @Size(min = 6, message = "Şifre en az 6 karakter olmalıdır")
    @Column(nullable = false)
    private String password;

    @NotBlank(message = "Telefon alanı boş olamaz")
    @Column(nullable = false)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    // Doktor için ek bilgiler
    private String specialization; // Uzmanlık alanı
    private String licenseNumber; // Diploma numarası

    // İlişkiler
    @OneToMany(mappedBy = "patient", cascade = CascadeType.ALL)
    private List<Appointment> patientAppointments;

    @OneToMany(mappedBy = "doctor", cascade = CascadeType.ALL)
    private List<Appointment> doctorAppointments;

    // Constructors
    public User() {
        this.createdAt = LocalDateTime.now();
    }

    public User(String firstName, String lastName, String email, String password, String phone, Role role) {
        this();
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.phone = phone;
        this.role = role;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getSpecialization() { return specialization; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }

    public String getLicenseNumber() { return licenseNumber; }
    public void setLicenseNumber(String licenseNumber) { this.licenseNumber = licenseNumber; }

    public List<Appointment> getPatientAppointments() { return patientAppointments; }
    public void setPatientAppointments(List<Appointment> patientAppointments) { this.patientAppointments = patientAppointments; }

    public List<Appointment> getDoctorAppointments() { return doctorAppointments; }
    public void setDoctorAppointments(List<Appointment> doctorAppointments) { this.doctorAppointments = doctorAppointments; }

    // FullName metodu - Template'lerde kullanılıyor
    public String getFullName() {
        if (firstName != null && lastName != null) {
            return firstName + " " + lastName;
        }
        return email; // Fallback olarak email döner
    }

    // setFullName metodu - Eğer template'te kullanılıyorsa
    public void setFullName(String fullName) {
        if (fullName != null && fullName.contains(" ")) {
            String[] parts = fullName.split(" ", 2);
            this.firstName = parts[0];
            this.lastName = parts[1];
        }
    }
}
