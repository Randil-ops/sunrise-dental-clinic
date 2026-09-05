package com.sunrise.dental.service;

import com.sunrise.dental.config.TreatmentPricingConfig;
import com.sunrise.dental.exception.DoubleBookingException;
import com.sunrise.dental.exception.ValidationException;
import com.sunrise.dental.model.Appointment;
import com.sunrise.dental.repository.AppointmentRepository;
import com.sunrise.dental.repository.StaffUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Covers the three core behaviours called out in the coursework brief:
 * successful registration, rejected double-booking, and correct bill total.
 * @Transactional rolls back each test so they don't pollute the shared
 * file-based H2 database used by the running application.
 */
@SpringBootTest
@Transactional
class AppointmentServiceTest {

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private StaffUserRepository staffUserRepository;

    @BeforeEach
    void ensureAdminExists() {
        if (staffUserRepository.findByUsername("admin").isEmpty()) {
            staffUserRepository.save(new com.sunrise.dental.model.StaffUser("admin", "admin123", "ADMIN"));
        }
    }

    private Appointment sampleAppointment(String dentist, LocalDate date, LocalTime time) {
        Appointment appointment = new Appointment();
        appointment.setPatientName("Kasun Perera");
        appointment.setAddress("12 Galle Road, Colombo");
        appointment.setContactNo("0771234567");
        appointment.setDentistName(dentist);
        appointment.setTreatmentType("Scaling & Cleaning");
        appointment.setAppointmentDate(date);
        appointment.setAppointmentTime(time);
        return appointment;
    }

    @Test
    void registerAppointment_succeedsAndGeneratesAppointmentNumber() {
        Appointment saved = appointmentService.registerAppointment(
                sampleAppointment("Dr. Fernando", LocalDate.now().plusDays(1), LocalTime.of(10, 0)));

        assertTrue(saved.getAppointmentNo().matches("APT-\\d{4}"));
        assertTrue(appointmentService.getByAppointmentNo(saved.getAppointmentNo()).isPresent());
    }

    @Test
    void registerAppointment_rejectsInvalidContactNumber() {
        Appointment appointment = sampleAppointment("Dr. Silva", LocalDate.now().plusDays(2), LocalTime.of(11, 0));
        appointment.setContactNo("123");

        assertThrows(ValidationException.class, () -> appointmentService.registerAppointment(appointment));
    }

    @Test
    void registerAppointment_rejectsDoubleBookingForSameDentistDateAndTime() {
        LocalDate date = LocalDate.now().plusDays(3);
        LocalTime time = LocalTime.of(14, 30);

        appointmentService.registerAppointment(sampleAppointment("Dr. Jayasuriya", date, time));

        Appointment clashing = sampleAppointment("Dr. Jayasuriya", date, time);
        assertThrows(DoubleBookingException.class, () -> appointmentService.registerAppointment(clashing));
    }

    @Test
    void generateBill_calculatesConsultationFeePlusTreatmentPrice() {
        Appointment saved = appointmentService.registerAppointment(
                sampleAppointment("Dr. Weerasinghe", LocalDate.now().plusDays(4), LocalTime.of(9, 15)));

        Appointment billed = appointmentService.generateBill(saved.getAppointmentNo());

        double expected = TreatmentPricingConfig.CONSULTATION_FEE + 3000.00; // Scaling & Cleaning price
        assertEquals(expected, billed.getTotalCost(), 0.001);
        assertTrue(billed.isBilled());
    }

    // TDD cycle: written before the operating-hours rule existed in AppointmentService.
    // Expected to FAIL (red) until validate() is extended to check clinic hours.
    @Test
    void registerAppointment_rejectsAppointmentOutsideClinicOperatingHours() {
        Appointment appointment = sampleAppointment("Dr. Fonseka", LocalDate.now().plusDays(5), LocalTime.of(20, 0));

        assertThrows(ValidationException.class, () -> appointmentService.registerAppointment(appointment));
    }
}
