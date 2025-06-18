package com.hospital.controller;

import com.hospital.entity.Appointment;
import com.hospital.entity.AppointmentStatus;
import com.hospital.entity.Role;
import com.hospital.entity.User;
import com.hospital.service.AppointmentService;
import com.hospital.service.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.time.LocalDateTime;
import java.util.Optional;

@Controller
@RequestMapping("/appointments")
public class AppointmentController {

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private UserService userService;

    /**
     * Randevu alma sayfası (hastalar için)
     */
    @GetMapping("/new")
    public String newAppointmentPage(HttpSession session, Model model) {
        User currentUser = (User) session.getAttribute("currentUser");

        System.out.println("New appointment page requested");

        if (currentUser == null) {
            System.out.println("No user in session");
            return "redirect:/login";
        }

        if (currentUser.getRole() != Role.PATIENT) {
            System.out.println("User is not patient: " + currentUser.getRole());
            return "redirect:/dashboard";
        }

        System.out.println("Loading doctors for appointment");
        model.addAttribute("appointment", new Appointment());
        model.addAttribute("doctors", userService.getAllDoctors());
        model.addAttribute("currentUser", currentUser);

        return "appointments/new";
    }

    /**
     * Randevu oluşturma
     */
    @PostMapping("/create")
    public String createAppointment(@RequestParam Long doctorId,
                                    @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime appointmentDateTime,
                                    @RequestParam(required = false) String description,
                                    HttpSession session,
                                    Model model,
                                    RedirectAttributes redirectAttributes) {

        User currentUser = (User) session.getAttribute("currentUser");

        System.out.println("Create appointment request received");
        System.out.println("Doctor ID: " + doctorId);
        System.out.println("Appointment DateTime: " + appointmentDateTime);
        System.out.println("Description: " + description);

        if (currentUser == null) {
            return "redirect:/login";
        }

        if (currentUser.getRole() != Role.PATIENT) {
            return "redirect:/dashboard";
        }

        try {
            // Doktor bulma
            Optional<User> doctorOpt = userService.findById(doctorId);
            if (!doctorOpt.isPresent()) {
                throw new RuntimeException("Doktor bulunamadı!");
            }

            User doctor = doctorOpt.get();
            if (doctor.getRole() != Role.DOCTOR) {
                throw new RuntimeException("Seçilen kullanıcı doktor değil!");
            }

            System.out.println("Doctor found: " + doctor.getFullName());

            // Randevu oluşturma
            Appointment appointment = new Appointment();
            appointment.setPatient(currentUser);
            appointment.setDoctor(doctor);
            appointment.setAppointmentDateTime(appointmentDateTime);
            appointment.setDescription(description);
            appointment.setStatus(AppointmentStatus.PENDING);

            System.out.println("Creating appointment...");
            Appointment savedAppointment = appointmentService.createAppointment(appointment);
            System.out.println("Appointment created with ID: " + savedAppointment.getId());

            redirectAttributes.addFlashAttribute("success", "Randevunuz başarıyla oluşturuldu!");
            return "redirect:/dashboard";

        } catch (RuntimeException e) {
            System.out.println("Error creating appointment: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", e.getMessage());
            model.addAttribute("doctors", userService.getAllDoctors());
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("appointment", new Appointment());
            return "appointments/new";
        }
    }

    /**
     * Randevu listesi
     */
    @GetMapping("/list")
    public String listAppointments(HttpSession session, Model model,
                                   @RequestParam(required = false) String status) {
        User currentUser = (User) session.getAttribute("currentUser");

        System.out.println("Appointment list requested");

        if (currentUser == null) {
            return "redirect:/login";
        }

        model.addAttribute("currentUser", currentUser);

        try {
            if (currentUser.getRole() == Role.PATIENT) {
                System.out.println("Loading patient appointments");
                if (status != null && !status.isEmpty()) {
                    try {
                        AppointmentStatus appointmentStatus = AppointmentStatus.valueOf(status.toUpperCase());
                        model.addAttribute("appointments",
                                appointmentService.getPatientAppointmentsByStatus(currentUser, appointmentStatus));
                    } catch (IllegalArgumentException e) {
                        model.addAttribute("appointments",
                                appointmentService.getPatientAppointments(currentUser));
                    }
                } else {
                    model.addAttribute("appointments",
                            appointmentService.getPatientAppointments(currentUser));
                }
            } else if (currentUser.getRole() == Role.DOCTOR) {
                System.out.println("Loading doctor appointments");
                if (status != null && !status.isEmpty()) {
                    try {
                        AppointmentStatus appointmentStatus = AppointmentStatus.valueOf(status.toUpperCase());
                        model.addAttribute("appointments",
                                appointmentService.getDoctorAppointmentsByStatus(currentUser, appointmentStatus));
                    } catch (IllegalArgumentException e) {
                        model.addAttribute("appointments",
                                appointmentService.getDoctorAppointments(currentUser));
                    }
                } else {
                    model.addAttribute("appointments",
                            appointmentService.getDoctorAppointments(currentUser));
                }
            }

            model.addAttribute("selectedStatus", status);
            model.addAttribute("statuses", AppointmentStatus.values());

        } catch (Exception e) {
            System.out.println("Error loading appointments: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Randevular yüklenirken hata oluştu: " + e.getMessage());
        }

        return "appointments/list";
    }

    /**
     * Randevu detayları
     */
    @GetMapping("/{id}")
    public String appointmentDetails(@PathVariable Long id, HttpSession session, Model model) {
        User currentUser = (User) session.getAttribute("currentUser");

        if (currentUser == null) {
            return "redirect:/login";
        }

        Optional<Appointment> appointmentOpt = appointmentService.findById(id);

        if (!appointmentOpt.isPresent()) {
            return "redirect:/appointments/list";
        }

        Appointment appointment = appointmentOpt.get();

        // Yetki kontrolü
        if (currentUser.getRole() == Role.PATIENT &&
                !appointment.getPatient().getId().equals(currentUser.getId())) {
            return "redirect:/dashboard";
        }

        if (currentUser.getRole() == Role.DOCTOR &&
                !appointment.getDoctor().getId().equals(currentUser.getId())) {
            return "redirect:/dashboard";
        }

        model.addAttribute("appointment", appointment);
        model.addAttribute("currentUser", currentUser);

        return "appointments/details";
    }

    /**
     * Randevu onaylama (doktorlar için)
     */
    @PostMapping("/{id}/approve")
    public String approveAppointment(@PathVariable Long id,
                                     @RequestParam(required = false) String doctorNotes,
                                     HttpSession session,
                                     RedirectAttributes redirectAttributes) {

        User currentUser = (User) session.getAttribute("currentUser");

        if (currentUser == null) {
            return "redirect:/login";
        }

        if (currentUser.getRole() != Role.DOCTOR) {
            return "redirect:/dashboard";
        }

        try {
            System.out.println("Approving appointment with ID: " + id);
            appointmentService.approveAppointment(id, doctorNotes);
            redirectAttributes.addFlashAttribute("success", "Randevu onaylandı!");
        } catch (RuntimeException e) {
            System.out.println("Error approving appointment: " + e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/appointments/" + id;
    }

    /**
     * Randevu reddetme (doktorlar için)
     */
    @PostMapping("/{id}/reject")
    public String rejectAppointment(@PathVariable Long id,
                                    @RequestParam(required = false) String doctorNotes,
                                    HttpSession session,
                                    RedirectAttributes redirectAttributes) {

        User currentUser = (User) session.getAttribute("currentUser");

        if (currentUser == null) {
            return "redirect:/login";
        }

        if (currentUser.getRole() != Role.DOCTOR) {
            return "redirect:/dashboard";
        }

        try {
            System.out.println("Rejecting appointment with ID: " + id);
            appointmentService.rejectAppointment(id, doctorNotes);
            redirectAttributes.addFlashAttribute("success", "Randevu reddedildi!");
        } catch (RuntimeException e) {
            System.out.println("Error rejecting appointment: " + e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/appointments/" + id;
    }

    /**
     * Randevu tamamlama (doktorlar için)
     */
    @PostMapping("/{id}/complete")
    public String completeAppointment(@PathVariable Long id,
                                      @RequestParam(required = false) String doctorNotes,
                                      HttpSession session,
                                      RedirectAttributes redirectAttributes) {

        User currentUser = (User) session.getAttribute("currentUser");

        if (currentUser == null) {
            return "redirect:/login";
        }

        if (currentUser.getRole() != Role.DOCTOR) {
            return "redirect:/dashboard";
        }

        try {
            System.out.println("Completing appointment with ID: " + id);
            appointmentService.completeAppointment(id, doctorNotes);
            redirectAttributes.addFlashAttribute("success", "Randevu tamamlandı!");
        } catch (RuntimeException e) {
            System.out.println("Error completing appointment: " + e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/appointments/" + id;
    }

    /**
     * Randevu iptal etme - Düzeltilmiş versiyon
     */
    @PostMapping("/{id}/cancel")
    public String cancelAppointment(@PathVariable Long id,
                                    HttpSession session,
                                    RedirectAttributes redirectAttributes) {

        User currentUser = (User) session.getAttribute("currentUser");

        System.out.println("=== RANDEVU İPTAL İŞLEMİ BAŞLADI ===");
        System.out.println("Appointment ID: " + id);
        System.out.println("Current User: " + (currentUser != null ? currentUser.getEmail() : "null"));

        if (currentUser == null) {
            System.out.println("User not logged in");
            return "redirect:/login";
        }

        try {
            // Randevuyu bul
            Optional<Appointment> appointmentOpt = appointmentService.findById(id);

            if (!appointmentOpt.isPresent()) {
                System.out.println("Appointment not found with ID: " + id);
                throw new RuntimeException("Randevu bulunamadı!");
            }

            Appointment appointment = appointmentOpt.get();
            System.out.println("Appointment found:");
            System.out.println("  Patient: " + appointment.getPatient().getEmail());
            System.out.println("  Doctor: " + appointment.getDoctor().getEmail());
            System.out.println("  Status: " + appointment.getStatus());
            System.out.println("  DateTime: " + appointment.getAppointmentDateTime());

            // Yetki kontrolü - Sadece hasta kendi randevusunu iptal edebilir
            if (currentUser.getRole() == Role.PATIENT) {
                if (!appointment.getPatient().getId().equals(currentUser.getId())) {
                    System.out.println("Patient authorization failed");
                    System.out.println("  Appointment patient ID: " + appointment.getPatient().getId());
                    System.out.println("  Current user ID: " + currentUser.getId());
                    throw new RuntimeException("Bu randevuyu iptal etme yetkiniz yok!");
                }
            } else if (currentUser.getRole() == Role.DOCTOR) {
                // Doktor da kendi randevusunu iptal edebilir
                if (!appointment.getDoctor().getId().equals(currentUser.getId())) {
                    System.out.println("Doctor authorization failed");
                    throw new RuntimeException("Bu randevuyu iptal etme yetkiniz yok!");
                }
            } else {
                System.out.println("Invalid user role: " + currentUser.getRole());
                throw new RuntimeException("Geçersiz kullanıcı rolü!");
            }

            // Durum kontrolü - Sadece belirli durumlarda iptal edilebilir
            if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
                throw new RuntimeException("Bu randevu zaten iptal edilmiş!");
            }

            if (appointment.getStatus() == AppointmentStatus.COMPLETED) {
                throw new RuntimeException("Tamamlanmış randevu iptal edilemez!");
            }

            System.out.println("Authorization successful, cancelling appointment...");

            // Randevuyu iptal et
            appointmentService.cancelAppointment(id);

            System.out.println("Appointment successfully cancelled");
            redirectAttributes.addFlashAttribute("success", "Randevu başarıyla iptal edildi!");

        } catch (RuntimeException e) {
            System.out.println("Error cancelling appointment: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        System.out.println("=== RANDEVU İPTAL İŞLEMİ TAMAMLANDI ===");
        return "redirect:/appointments/list";
    }
}