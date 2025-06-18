package com.hospital.controller;

import com.hospital.entity.AppointmentStatus;
import com.hospital.entity.Role;
import com.hospital.entity.User;
import com.hospital.service.AppointmentService;
import com.hospital.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private UserService userService;

    /**
     * Ana dashboard sayfası
     */
    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        User currentUser = (User) session.getAttribute("currentUser");

        System.out.println("Dashboard request received");

        if (currentUser == null) {
            System.out.println("No user in session, redirecting to login");
            return "redirect:/login";
        }

        System.out.println("User in session: " + currentUser.getEmail() + " - Role: " + currentUser.getRole());

        model.addAttribute("currentUser", currentUser);

        try {
            if (currentUser.getRole() == Role.PATIENT) {
                System.out.println("Loading patient dashboard");

                model.addAttribute("myAppointments",
                        appointmentService.getPatientAppointments(currentUser));
                model.addAttribute("pendingAppointments",
                        appointmentService.getPatientAppointmentsByStatus(currentUser, AppointmentStatus.PENDING));
                model.addAttribute("approvedAppointments",
                        appointmentService.getPatientAppointmentsByStatus(currentUser, AppointmentStatus.APPROVED));

                return "dashboard/patient-dashboard";

            } else if (currentUser.getRole() == Role.DOCTOR) {
                System.out.println("Loading doctor dashboard");

                model.addAttribute("myAppointments",
                        appointmentService.getDoctorAppointments(currentUser));
                model.addAttribute("pendingAppointments",
                        appointmentService.getDoctorAppointmentsByStatus(currentUser, AppointmentStatus.PENDING));
                model.addAttribute("approvedAppointments",
                        appointmentService.getDoctorAppointmentsByStatus(currentUser, AppointmentStatus.APPROVED));

                return "dashboard/doctor-dashboard";
            }
        } catch (Exception e) {
            System.out.println("Error in dashboard: " + e.getMessage());
            e.printStackTrace();
        }

        return "redirect:/";
    }

    /**
     * İstatistikler sayfası (SADECE DOKTORLAR İÇİN)
     */
    @GetMapping("/dashboard/stats")
    public String stats(HttpSession session, Model model) {
        User currentUser = (User) session.getAttribute("currentUser");

        System.out.println("=== STATS PAGE REQUEST ===");
        System.out.println("Current user: " + (currentUser != null ? currentUser.getEmail() : "null"));

        if (currentUser == null) {
            System.out.println("No user in session, redirecting to login");
            return "redirect:/login";
        }

        System.out.println("User role: " + currentUser.getRole());

        // SADECE DOKTORLAR ERİŞEBİLİR
        if (currentUser.getRole() != Role.DOCTOR) {
            System.out.println("User is not doctor, redirecting to dashboard");
            return "redirect:/dashboard";
        }

        System.out.println("Doctor authorized, loading stats...");

        try {
            // İstatistik verilerini topla
            long totalPatients = userService.getTotalPatients();
            long totalDoctors = userService.getTotalDoctors();
            long totalAppointments = appointmentService.getTotalAppointments();
            long pendingAppointments = appointmentService.getPendingAppointments();
            long approvedAppointments = appointmentService.getApprovedAppointments();
            long completedAppointments = appointmentService.getCompletedAppointments();

            System.out.println("Stats loaded:");
            System.out.println("  Total Patients: " + totalPatients);
            System.out.println("  Total Doctors: " + totalDoctors);
            System.out.println("  Total Appointments: " + totalAppointments);
            System.out.println("  Pending: " + pendingAppointments);
            System.out.println("  Approved: " + approvedAppointments);
            System.out.println("  Completed: " + completedAppointments);

            // Model'e verileri ekle
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("totalPatients", totalPatients);
            model.addAttribute("totalDoctors", totalDoctors);
            model.addAttribute("totalAppointments", totalAppointments);
            model.addAttribute("pendingAppointments", pendingAppointments);
            model.addAttribute("approvedAppointments", approvedAppointments);
            model.addAttribute("completedAppointments", completedAppointments);

            System.out.println("Returning dashboard/stats template");
            return "dashboard/stats";

        } catch (Exception e) {
            System.out.println("Error loading stats: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "İstatistikler yüklenirken hata oluştu: " + e.getMessage());
            return "dashboard/doctor-dashboard";
        }
    }
}