package com.hospital.service;

import com.hospital.entity.Appointment;
import com.hospital.entity.AppointmentStatus;
import com.hospital.entity.User;
import com.hospital.repository.AppointmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
public class AppointmentService {

    @Autowired
    private AppointmentRepository appointmentRepository;

    /**
     * Randevu oluşturma - Detaylı çakışma kontrolü ile
     */
    public Appointment createAppointment(Appointment appointment) {
        System.out.println("=== RANDEVU OLUŞTURMA BAŞLADI ===");

        // Geçmiş tarih kontrolü
        if (appointment.getAppointmentDateTime().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Geçmiş tarihe randevu alınamaz!");
        }

        // Çakışma kontrolü
        String conflictMessage = checkTimeConflict(appointment);
        if (conflictMessage != null) {
            throw new RuntimeException(conflictMessage);
        }

        // Varsayılan değerleri ayarla
        if (appointment.getStatus() == null) {
            appointment.setStatus(AppointmentStatus.PENDING);
        }

        if (appointment.getCreatedAt() == null) {
            appointment.setCreatedAt(LocalDateTime.now());
        }

        appointment.setUpdatedAt(LocalDateTime.now());

        System.out.println("Saving appointment to database");
        System.out.println("Patient: " + appointment.getPatient().getFullName());
        System.out.println("Doctor: " + appointment.getDoctor().getFullName());
        System.out.println("DateTime: " + appointment.getAppointmentDateTime());
        System.out.println("Status: " + appointment.getStatus());

        Appointment savedAppointment = appointmentRepository.save(appointment);
        System.out.println("Appointment saved with ID: " + savedAppointment.getId());
        System.out.println("=== RANDEVU OLUŞTURMA TAMAMLANDI ===");

        return savedAppointment;
    }

    /**
     * Çakışma kontrolü - Hem doktor hem hasta için
     */
    private String checkTimeConflict(Appointment newAppointment) {
        System.out.println("\n--- ÇAKIŞMA KONTROLÜ BAŞLADI ---");

        LocalDateTime newStart = newAppointment.getAppointmentDateTime();
        LocalDateTime newEnd = newStart.plusMinutes(60); // 1 saatlik randevu

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

        System.out.println("Yeni randevu:");
        System.out.println("  Hasta: " + newAppointment.getPatient().getFullName());
        System.out.println("  Doktor: " + newAppointment.getDoctor().getFullName());
        System.out.println("  Başlangıç: " + newStart.format(formatter));
        System.out.println("  Bitiş: " + newEnd.format(formatter));

        // 1. DOKTOR ÇAKIŞMA KONTROLÜ
        System.out.println("\n=== DOKTOR ÇAKIŞMA KONTROLÜ ===");
        String doctorConflict = checkDoctorConflict(newAppointment, newStart, newEnd, formatter);
        if (doctorConflict != null) {
            return doctorConflict;
        }

        // 2. HASTA ÇAKIŞMA KONTROLÜ
        System.out.println("\n=== HASTA ÇAKIŞMA KONTROLÜ ===");
        String patientConflict = checkPatientConflict(newAppointment, newStart, newEnd, formatter);
        if (patientConflict != null) {
            return patientConflict;
        }

        System.out.println("Hem doktor hem hasta için çakışma yok - randevu oluşturulabilir");
        System.out.println("--- ÇAKIŞMA KONTROLÜ TAMAMLANDI ---\n");
        return null; // Çakışma yok
    }

    /**
     * Doktor çakışma kontrolü
     */
    private String checkDoctorConflict(Appointment newAppointment, LocalDateTime newStart,
                                       LocalDateTime newEnd, DateTimeFormatter formatter) {

        List<Appointment> doctorAppointments = appointmentRepository.findByDoctorOrderByAppointmentDateTimeDesc(newAppointment.getDoctor());
        System.out.println("Doktorun toplam randevu sayısı: " + doctorAppointments.size());

        List<Appointment> activeDoctorAppointments = doctorAppointments.stream()
                .filter(appointment -> !appointment.getStatus().equals(AppointmentStatus.CANCELLED))
                .toList();

        System.out.println("Doktorun aktif randevu sayısı: " + activeDoctorAppointments.size());

        if (activeDoctorAppointments.isEmpty()) {
            System.out.println("Doktorun hiç aktif randevusu yok");
            return null;
        }

        for (int i = 0; i < activeDoctorAppointments.size(); i++) {
            Appointment existing = activeDoctorAppointments.get(i);
            LocalDateTime existingStart = existing.getAppointmentDateTime();
            LocalDateTime existingEnd = existingStart.plusMinutes(60);

            System.out.println("\nDoktor randevusu " + (i + 1) + ":");
            System.out.println("  ID: " + existing.getId());
            System.out.println("  Hasta: " + existing.getPatient().getFullName());
            System.out.println("  Durum: " + existing.getStatus().getDisplayName());
            System.out.println("  Başlangıç: " + existingStart.format(formatter));
            System.out.println("  Bitiş: " + existingEnd.format(formatter));

            boolean conflicts = isTimeOverlapping(newStart, newEnd, existingStart, existingEnd);
            System.out.println("  Doktor çakışması: " + (conflicts ? "EVET" : "HAYIR"));

            if (conflicts) {
                System.out.println("*** DOKTOR ÇAKIŞMASI TESPİT EDİLDİ! ***");
                System.out.println("--- ÇAKIŞMA KONTROLÜ TAMAMLANDI ---\n");
                return "Bu saatte (" + newStart.format(formatter) + ") doktorun başka bir randevusu bulunmaktadır!";
            }
        }

        System.out.println("Doktor için çakışma yok");
        return null;
    }

    /**
     * Hasta çakışma kontrolü
     */
    private String checkPatientConflict(Appointment newAppointment, LocalDateTime newStart,
                                        LocalDateTime newEnd, DateTimeFormatter formatter) {

        List<Appointment> patientAppointments = appointmentRepository.findByPatientOrderByAppointmentDateTimeDesc(newAppointment.getPatient());
        System.out.println("Hastanın toplam randevu sayısı: " + patientAppointments.size());

        List<Appointment> activePatientAppointments = patientAppointments.stream()
                .filter(appointment -> !appointment.getStatus().equals(AppointmentStatus.CANCELLED))
                .toList();

        System.out.println("Hastanın aktif randevu sayısı: " + activePatientAppointments.size());

        if (activePatientAppointments.isEmpty()) {
            System.out.println("Hastanın hiç aktif randevusu yok");
            return null;
        }

        for (int i = 0; i < activePatientAppointments.size(); i++) {
            Appointment existing = activePatientAppointments.get(i);
            LocalDateTime existingStart = existing.getAppointmentDateTime();
            LocalDateTime existingEnd = existingStart.plusMinutes(60);

            System.out.println("\nHasta randevusu " + (i + 1) + ":");
            System.out.println("  ID: " + existing.getId());
            System.out.println("  Doktor: " + existing.getDoctor().getFullName());
            System.out.println("  Durum: " + existing.getStatus().getDisplayName());
            System.out.println("  Başlangıç: " + existingStart.format(formatter));
            System.out.println("  Bitiş: " + existingEnd.format(formatter));

            boolean conflicts = isTimeOverlapping(newStart, newEnd, existingStart, existingEnd);
            System.out.println("  Hasta çakışması: " + (conflicts ? "EVET" : "HAYIR"));

            if (conflicts) {
                System.out.println("*** HASTA ÇAKIŞMASI TESPİT EDİLDİ! ***");
                System.out.println("--- ÇAKIŞMA KONTROLÜ TAMAMLANDI ---\n");
                return "Bu saatte (" + newStart.format(formatter) + ") zaten başka bir randevunuz bulunmaktadır!";
            }
        }

        System.out.println("Hasta için çakışma yok");
        return null;
    }

    /**
     * İki zaman aralığının çakışıp çakışmadığını kontrol eder
     */
    private boolean isTimeOverlapping(LocalDateTime start1, LocalDateTime end1,
                                      LocalDateTime start2, LocalDateTime end2) {

        // Aynı dakika kontrolü (tam eşitlik)
        if (start1.equals(start2)) {
            System.out.println("    → Başlangıç saatleri aynı");
            return true;
        }

        // Çakışma kontrolü: start1 < end2 && start2 < end1
        boolean overlaps = start1.isBefore(end2) && start2.isBefore(end1);

        if (overlaps) {
            System.out.println("    → Zaman aralıkları çakışıyor");
            System.out.println("    → Kontrol: " + start1 + " < " + end2 + " = " + start1.isBefore(end2));
            System.out.println("    → Kontrol: " + start2 + " < " + end1 + " = " + start2.isBefore(end1));
        } else {
            System.out.println("    → Zaman aralıkları çakışmıyor");
        }

        return overlaps;
    }

    /**
     * Hasta randevularını getirme
     */
    public List<Appointment> getPatientAppointments(User patient) {
        System.out.println("Getting appointments for patient: " + patient.getEmail());
        List<Appointment> appointments = appointmentRepository.findByPatientOrderByAppointmentDateTimeDesc(patient);
        System.out.println("Found " + appointments.size() + " appointments for patient");
        return appointments;
    }

    /**
     * Doktor randevularını getirme
     */
    public List<Appointment> getDoctorAppointments(User doctor) {
        System.out.println("Getting appointments for doctor: " + doctor.getEmail());
        List<Appointment> appointments = appointmentRepository.findByDoctorOrderByAppointmentDateTimeDesc(doctor);
        System.out.println("Found " + appointments.size() + " appointments for doctor");
        return appointments;
    }

    /**
     * Durum ile randevuları getirme
     */
    public List<Appointment> getAppointmentsByStatus(AppointmentStatus status) {
        return appointmentRepository.findByStatus(status);
    }

    /**
     * Doktor ve durum ile randevuları getirme
     */
    public List<Appointment> getDoctorAppointmentsByStatus(User doctor, AppointmentStatus status) {
        return appointmentRepository.findByDoctorAndStatus(doctor, status);
    }

    /**
     * Hasta ve durum ile randevuları getirme
     */
    public List<Appointment> getPatientAppointmentsByStatus(User patient, AppointmentStatus status) {
        return appointmentRepository.findByPatientAndStatus(patient, status);
    }

    /**
     * Randevu onaylama
     */
    public Appointment approveAppointment(Long appointmentId, String doctorNotes) {
        Optional<Appointment> appointmentOpt = appointmentRepository.findById(appointmentId);

        if (appointmentOpt.isPresent()) {
            Appointment appointment = appointmentOpt.get();
            appointment.setStatus(AppointmentStatus.APPROVED);
            if (doctorNotes != null && !doctorNotes.trim().isEmpty()) {
                appointment.setDoctorNotes(doctorNotes);
            }
            appointment.setUpdatedAt(LocalDateTime.now());
            return appointmentRepository.save(appointment);
        }

        throw new RuntimeException("Randevu bulunamadı!");
    }

    /**
     * Randevu reddetme
     */
    public Appointment rejectAppointment(Long appointmentId, String doctorNotes) {
        Optional<Appointment> appointmentOpt = appointmentRepository.findById(appointmentId);

        if (appointmentOpt.isPresent()) {
            Appointment appointment = appointmentOpt.get();
            appointment.setStatus(AppointmentStatus.REJECTED);
            if (doctorNotes != null && !doctorNotes.trim().isEmpty()) {
                appointment.setDoctorNotes(doctorNotes);
            }
            appointment.setUpdatedAt(LocalDateTime.now());
            return appointmentRepository.save(appointment);
        }

        throw new RuntimeException("Randevu bulunamadı!");
    }

    /**
     * Randevu tamamlama
     */
    public Appointment completeAppointment(Long appointmentId, String doctorNotes) {
        Optional<Appointment> appointmentOpt = appointmentRepository.findById(appointmentId);

        if (appointmentOpt.isPresent()) {
            Appointment appointment = appointmentOpt.get();
            appointment.setStatus(AppointmentStatus.COMPLETED);
            if (doctorNotes != null && !doctorNotes.trim().isEmpty()) {
                appointment.setDoctorNotes(doctorNotes);
            }
            appointment.setUpdatedAt(LocalDateTime.now());
            return appointmentRepository.save(appointment);
        }

        throw new RuntimeException("Randevu bulunamadı!");
    }

    /**
     * Randevu iptal etme - Debug logları ile
     */
    public Appointment cancelAppointment(Long appointmentId) {
        System.out.println("=== SERVİS: RANDEVU İPTAL ===");
        System.out.println("Appointment ID: " + appointmentId);

        Optional<Appointment> appointmentOpt = appointmentRepository.findById(appointmentId);

        if (appointmentOpt.isPresent()) {
            Appointment appointment = appointmentOpt.get();

            System.out.println("Appointment found, current status: " + appointment.getStatus());

            appointment.setStatus(AppointmentStatus.CANCELLED);
            appointment.setUpdatedAt(LocalDateTime.now());

            System.out.println("Saving appointment with CANCELLED status");
            Appointment savedAppointment = appointmentRepository.save(appointment);

            System.out.println("Appointment saved successfully with status: " + savedAppointment.getStatus());
            System.out.println("=== SERVİS: İPTAL TAMAMLANDI ===");

            return savedAppointment;
        }

        System.out.println("Appointment not found in database!");
        throw new RuntimeException("Randevu bulunamadı!");
    }

    /**
     * ID ile randevu bulma
     */
    public Optional<Appointment> findById(Long id) {
        return appointmentRepository.findById(id);
    }

    /**
     * Tarih aralığında randevular
     */
    public List<Appointment> getAppointmentsBetweenDates(LocalDateTime startDate, LocalDateTime endDate) {
        return appointmentRepository.findAppointmentsBetweenDates(startDate, endDate);
    }

    /**
     * Randevu istatistikleri
     */
    public long getTotalAppointments() {
        return appointmentRepository.count();
    }

    public long getPendingAppointments() {
        return appointmentRepository.countByStatus(AppointmentStatus.PENDING);
    }

    public long getApprovedAppointments() {
        return appointmentRepository.countByStatus(AppointmentStatus.APPROVED);
    }

    public long getCompletedAppointments() {
        return appointmentRepository.countByStatus(AppointmentStatus.COMPLETED);
    }
}