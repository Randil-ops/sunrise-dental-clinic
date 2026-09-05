package com.sunrise.dental.web;

import com.sunrise.dental.config.TreatmentPricingConfig;
import com.sunrise.dental.exception.AppointmentNotFoundException;
import com.sunrise.dental.exception.DoubleBookingException;
import com.sunrise.dental.exception.ValidationException;
import com.sunrise.dental.model.Appointment;
import com.sunrise.dental.service.AppointmentService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ClinicController {

    private final AppointmentService appointmentService;
    private final TreatmentPricingConfig pricingConfig;

    public ClinicController(AppointmentService appointmentService, TreatmentPricingConfig pricingConfig) {
        this.appointmentService = appointmentService;
        this.pricingConfig = pricingConfig;
    }

    @GetMapping({"/", "/home"})
    public String home(HttpSession session, Model model) {
        model.addAttribute("username", session.getAttribute(AuthController.SESSION_USER_KEY));
        return "home";
    }

    // ------------------------------------------------------------------
    // Register New Appointment
    // ------------------------------------------------------------------
    @GetMapping("/appointments/new")
    public String registerForm(Model model) {
        if (!model.containsAttribute("appointment")) {
            model.addAttribute("appointment", new Appointment());
        }
        model.addAttribute("treatments", pricingConfig.getAllTreatments().keySet());
        return "register";
    }

    @PostMapping("/appointments/new")
    public String registerSubmit(@ModelAttribute Appointment appointment, Model model) {
        try {
            Appointment saved = appointmentService.registerAppointment(appointment);
            model.addAttribute("success", "Appointment registered successfully! Your appointment number is "
                    + saved.getAppointmentNo());
            model.addAttribute("appointment", new Appointment());
        } catch (ValidationException | DoubleBookingException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("appointment", appointment);
        }
        model.addAttribute("treatments", pricingConfig.getAllTreatments().keySet());
        return "register";
    }

    // ------------------------------------------------------------------
    // Display Appointment Details
    // ------------------------------------------------------------------
    @GetMapping("/appointments/search")
    public String searchForm() {
        return "search";
    }

    @PostMapping("/appointments/search")
    public String searchSubmit(@RequestParam String appointmentNo, Model model) {
        model.addAttribute("appointmentNo", appointmentNo);
        appointmentService.getByAppointmentNo(appointmentNo).ifPresentOrElse(
                appointment -> model.addAttribute("appointment", appointment),
                () -> model.addAttribute("error", "No appointment found with number: " + appointmentNo)
        );
        return "search";
    }

    // ------------------------------------------------------------------
    // Calculate and Print Bill
    // ------------------------------------------------------------------
    @GetMapping("/bill")
    public String billForm() {
        return "bill";
    }

    @PostMapping("/bill")
    public String billSubmit(@RequestParam String appointmentNo, Model model) {
        try {
            Appointment billed = appointmentService.generateBill(appointmentNo);
            model.addAttribute("appointment", billed);
            model.addAttribute("consultationFee", TreatmentPricingConfig.CONSULTATION_FEE);
            model.addAttribute("treatmentFee", billed.getTotalCost() - TreatmentPricingConfig.CONSULTATION_FEE);
        } catch (AppointmentNotFoundException e) {
            model.addAttribute("error", e.getMessage());
        }
        return "bill";
    }

    // ------------------------------------------------------------------
    // Reports / Help
    // ------------------------------------------------------------------
    @GetMapping("/appointments")
    public String listAll(Model model) {
        model.addAttribute("appointments", appointmentService.listAll());
        return "list";
    }

    @GetMapping("/help")
    public String help() {
        return "help";
    }
}
