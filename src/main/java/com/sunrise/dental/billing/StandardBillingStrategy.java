package com.sunrise.dental.billing;

import com.sunrise.dental.config.TreatmentPricingConfig;
import org.springframework.stereotype.Component;

/**
 * Default (Strategy pattern) billing rule: a fixed clinic consultation
 * fee plus the price looked up for the given treatment type.
 */
@Component
public class StandardBillingStrategy implements BillingStrategy {

    private final TreatmentPricingConfig pricingConfig;

    public StandardBillingStrategy(TreatmentPricingConfig pricingConfig) {
        this.pricingConfig = pricingConfig;
    }

    @Override
    public double calculateTotal(String treatmentType) {
        return TreatmentPricingConfig.CONSULTATION_FEE + pricingConfig.getPriceFor(treatmentType);
    }
}
