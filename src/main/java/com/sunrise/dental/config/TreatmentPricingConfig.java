package com.sunrise.dental.config;

import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Central price list for every treatment type offered by the clinic.
 * Spring manages this as a singleton bean, so there is exactly one shared
 * pricing table for the whole application (Singleton pattern via the
 * Spring container, rather than a hand-rolled private-constructor class).
 */
@Component
public class TreatmentPricingConfig {

    public static final double CONSULTATION_FEE = 200.00;

    private final Map<String, Double> treatmentPrices = new LinkedHashMap<>();

    public TreatmentPricingConfig() {
        treatmentPrices.put("Consultation", 1500.00);
        treatmentPrices.put("Scaling & Cleaning", 3000.00);
        treatmentPrices.put("Tooth Filling", 5000.00);
        treatmentPrices.put("Tooth Extraction", 4000.00);
        treatmentPrices.put("Root Canal Treatment", 15000.00);
        treatmentPrices.put("Braces Consultation", 2500.00);
        treatmentPrices.put("Teeth Whitening", 8000.00);
    }

    public Map<String, Double> getAllTreatments() {
        return treatmentPrices;
    }

    public double getPriceFor(String treatmentType) {
        Double price = treatmentPrices.get(treatmentType);
        if (price == null) {
            throw new IllegalArgumentException("Unknown treatment type: " + treatmentType);
        }
        return price;
    }
}
