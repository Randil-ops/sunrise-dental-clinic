package com.sunrise.dental.billing;

/**
 * Strategy pattern: defines how a bill total is calculated for a treatment.
 * Swapping the implementation bound in Spring's context (e.g. for a future
 * discounted or insurance billing scheme) requires no change to
 * AppointmentService, which only depends on this interface.
 */
public interface BillingStrategy {
    double calculateTotal(String treatmentType);
}
