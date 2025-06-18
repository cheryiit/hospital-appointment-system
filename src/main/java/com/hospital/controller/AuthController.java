package com.hospital.controller;

import com.hospital.entity.Role;
import com.hospital.entity.User;
import com.hospital.service.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    @Autowired
    private UserService userService;

    @GetMapping("/")
    public String home() {
        return "index";
    }

    @GetMapping("/login")
    public String loginPage(HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser != null) {
            System.out.println("User already logged in, redirecting to dashboard");
            return "redirect:/dashboard";
        }
        return "auth/login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String email,
                        @RequestParam String password,
                        HttpSession session,
                        Model model) {
        try {
            System.out.println("Login attempt for: " + email);

            User user = userService.authenticateUser(email, password);
            session.setAttribute("currentUser", user);

            System.out.println("Login successful, redirecting to dashboard");
            return "redirect:/dashboard";

        } catch (RuntimeException e) {
            System.out.println("Login failed: " + e.getMessage());
            model.addAttribute("error", e.getMessage());
            return "auth/login";
        }
    }

    @GetMapping("/register")
    public String registerPage(Model model, HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser != null) {
            return "redirect:/dashboard";
        }
        model.addAttribute("user", new User());
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute User user,
                           BindingResult result,
                           @RequestParam String confirmPassword,
                           Model model,
                           RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            return "auth/register";
        }

        if (!user.getPassword().equals(confirmPassword)) {
            model.addAttribute("error", "Şifreler eşleşmiyor!");
            return "auth/register";
        }

        try {
            if (user.getRole() == null) {
                user.setRole(Role.PATIENT);
            }

            userService.registerUser(user);
            redirectAttributes.addFlashAttribute("success", "Kayıt başarılı! Şimdi giriş yapabilirsiniz.");
            return "redirect:/login";
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            return "auth/register";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session, RedirectAttributes redirectAttributes) {
        session.invalidate();
        redirectAttributes.addFlashAttribute("success", "Başarıyla çıkış yaptınız.");
        return "redirect:/";
    }
}