package com.sunrise.dental.repository;

import com.sunrise.dental.model.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    Optional<Appointment> findByAppointmentNo(String appointmentNo);

    boolean existsByDentistNameAndAppointmentDateAndAppointmentTime(
            String dentistName, LocalDate appointmentDate, LocalTime appointmentTime);

    List<Appointment> findByBilledFalse();
}
