package com.sunrise.dental.service;

import com.sunrise.dental.billing.BillingStrategy;
import com.sunrise.dental.exception.AppointmentNotFoundException;
import com.sunrise.dental.exception.DoubleBookingException;
import com.sunrise.dental.exception.ValidationException;
import com.sunrise.dental.model.Appointment;
import com.sunrise.dental.model.StaffUser;
import com.sunrise.dental.repository.AppointmentRepository;
import com.sunrise.dental.repository.StaffUserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

/**
 * Facade pattern: this is the single entry point the web controllers and
 * REST controllers both call into. It hides the coordination between the
 * repositories (Repository/DAO pattern, provided by Spring Data JPA) and
 * the billing strategy (Strategy pattern) behind a small, task-oriented API.
 */
@Service
public class AppointmentService {

    private static final LocalTime CLINIC_OPENING_TIME = LocalTime.of(8, 0);
    private static final LocalTime CLINIC_CLOSING_TIME = LocalTime.of(18, 0);

    private final AppointmentRepository appointmentRepository;
    private final StaffUserRepository staffUserRepository;
    private final BillingStrategy billingStrategy;

    public AppointmentService(AppointmentRepository appointmentRepository,
                               StaffUserRepository staffUserRepository,
                               BillingStrategy billingStrategy) {
        this.appointmentRepository = appointmentRepository;
        this.staffUserRepository = staffUserRepository;
        this.billingStrategy = billingStrategy;
    }

    // ------------------------------------------------------------------
    // Authentication
    // ------------------------------------------------------------------
    public Optional<StaffUser> login(String username, String password) {
        return staffUserRepository.findByUsername(username)
                .filter(user -> user.getPassword().equals(password));
    }

    // ------------------------------------------------------------------
    // Register New Appointment
    // ------------------------------------------------------------------
    public Appointment registerAppointment(Appointment appointment) {
        validate(appointment);

        boolean clash = appointmentRepository.existsByDentistNameAndAppointmentDateAndAppointmentTime(
                appointment.getDentistName(), appointment.getAppointmentDate(), appointment.getAppointmentTime());
        if (clash) {
            throw new DoubleBookingException("Dr. " + appointment.getDentistName()
                    + " already has an appointment booked at that date and time.");
        }

        appointment.setAppointmentNo(generateNextAppointmentNumber());
        appointment.setBilled(false);
        appointment.setTotalCost(0.0);
        return appointmentRepository.save(appointment);
    }

    private void validate(Appointment appointment) {
        if (isBlank(appointment.getPatientName())) {
            throw new ValidationException("Patient name is required.");
        }
        if (isBlank(appointment.getAddress())) {
            throw new ValidationException("Address is required.");
        }
        if (appointment.getContactNo() == null || !appointment.getContactNo().matches("\\d{10}")) {
            throw new ValidationException("Contact number must be exactly 10 digits.");
        }
        if (isBlank(appointment.getDentistName())) {
            throw new ValidationException("Dentist name is required.");
        }
        if (isBlank(appointment.getTreatmentType())) {
            throw new ValidationException("Treatment type is required.");
        }
        if (appointment.getAppointmentDate() == null || appointment.getAppointmentDate().isBefore(LocalDate.now())) {
            throw new ValidationException("Appointment date cannot be in the past.");
        }
        if (appointment.getAppointmentTime() == null) {
            throw new ValidationException("Appointment time is required.");
        }
        if (appointment.getAppointmentTime().isBefore(CLINIC_OPENING_TIME)
                || appointment.getAppointmentTime().isAfter(CLINIC_CLOSING_TIME)) {
            throw new ValidationException("Appointment time must be within clinic operating hours (08:00-18:00).");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String generateNextAppointmentNumber() {
        int max = appointmentRepository.findAll().stream()
                .map(Appointment::getAppointmentNo)
                .filter(no -> no != null && no.matches("APT-\\d+"))
                .mapToInt(no -> Integer.parseInt(no.substring(4)))
                .max()
                .orElse(0);
        return String.format("APT-%04d", max + 1);
    }

    // ------------------------------------------------------------------
    // Display Appointment Details
    // ------------------------------------------------------------------
    public Optional<Appointment> getByAppointmentNo(String appointmentNo) {
        return appointmentRepository.findByAppointmentNo(appointmentNo);
    }

    // ------------------------------------------------------------------
    // Calculate and Print Bill
    // ------------------------------------------------------------------
    public Appointment generateBill(String appointmentNo) {
        Appointment appointment = appointmentRepository.findByAppointmentNo(appointmentNo)
                .orElseThrow(() -> new AppointmentNotFoundException(
                        "No appointment found with number: " + appointmentNo));

        double total = billingStrategy.calculateTotal(appointment.getTreatmentType());
        appointment.setTotalCost(total);
        appointment.setBilled(true);
        return appointmentRepository.save(appointment);
    }

    // ------------------------------------------------------------------
    // Reports
    // ------------------------------------------------------------------
    public List<Appointment> listAll() {
        return appointmentRepository.findAll();
    }

    public List<Appointment> listUnbilled() {
        return appointmentRepository.findByBilledFalse();
    }
}
