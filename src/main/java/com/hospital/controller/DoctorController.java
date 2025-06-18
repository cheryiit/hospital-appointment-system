package com.hospital.controller;

import com.hospital.entity.Role;
import com.hospital.entity.User;
import com.hospital.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.List;

@Controller
@RequestMapping("/doctors")
// Ana servis sınıfı - iş mantığı burada yer alır
public class DoctorController {

    @Autowired
    private UserService userService;

    /**
     * Doktor listesi
     */
    @GetMapping("/list")
    public String listDoctors(HttpSession session, Model model,
                              @RequestParam(required = false) String search) {
        User currentUser = (User) session.getAttribute("currentUser");

        if (currentUser == null) {
            return "redirect:/login";
        }

        List<User> doctors;

        if (search != null && !search.trim().isEmpty()) {
            doctors = userService.searchDoctors(search);
            model.addAttribute("searchTerm", search);
        } else {
            doctors = userService.getAllDoctors();
        }

        model.addAttribute("doctors", doctors);
        model.addAttribute("currentUser", currentUser);

        return "doctors/list";
    }
}
