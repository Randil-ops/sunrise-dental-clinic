package com.sunrise.dental.web;

import com.sunrise.dental.model.StaffUser;
import com.sunrise.dental.service.AppointmentService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@Controller
public class AuthController {

    public static final String SESSION_USER_KEY = "loggedInUser";

    private final AppointmentService appointmentService;

    public AuthController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @GetMapping("/login")
    public String loginForm() {
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String username, @RequestParam String password,
                         HttpSession session, Model model) {
        Optional<StaffUser> user = appointmentService.login(username, password);
        if (user.isEmpty()) {
            model.addAttribute("error", "Invalid username or password.");
            model.addAttribute("username", username);
            return "login";
        }
        session.setAttribute(SESSION_USER_KEY, user.get().getUsername());
        return "redirect:/home";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login?loggedOut";
    }
}
