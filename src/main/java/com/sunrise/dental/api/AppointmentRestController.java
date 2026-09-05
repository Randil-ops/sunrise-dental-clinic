package com.sunrise.dental.api;

import com.sunrise.dental.model.Appointment;
import com.sunrise.dental.service.AppointmentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * JSON REST API exposing the same AppointmentService (Facade) used by the
 * Thymeleaf web UI, satisfying the "distributed application with web
 * services" requirement without duplicating any business logic.
 */
@RestController
@RequestMapping("/api")
public class AppointmentRestController {

    private final AppointmentService appointmentService;

    public AppointmentRestController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "UP"));
    }

    @GetMapping("/appointments")
    public ResponseEntity<List<Appointment>> listAll() {
        return ResponseEntity.ok(appointmentService.listAll());
    }

    @GetMapping("/appointments/{no}")
    public ResponseEntity<?> getByNumber(@PathVariable("no") String appointmentNo) {
        return appointmentService.getByAppointmentNo(appointmentNo)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "No appointment found with number: " + appointmentNo)));
    }

    @PostMapping("/appointments")
    public ResponseEntity<Appointment> create(@RequestBody Appointment appointment) {
        Appointment saved = appointmentService.registerAppointment(appointment);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PostMapping("/appointments/{no}/bill")
    public ResponseEntity<Appointment> bill(@PathVariable("no") String appointmentNo) {
        Appointment billed = appointmentService.generateBill(appointmentNo);
        return ResponseEntity.ok(billed);
    }
}
