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
import org.springframework.security.web.csrf.CsrfToken;
import jakarta.servlet.http.HttpServletRequest;

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
    public String dashboard(HttpSession session, Model model, HttpServletRequest request) {
        CsrfToken csrfToken = (CsrfToken) request.getAttribute("_csrf");
        model.addAttribute("_csrf", csrfToken);
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
}