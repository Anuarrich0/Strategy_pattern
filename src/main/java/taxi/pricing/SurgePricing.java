package taxi.pricing;

import taxi.demand.DemandSource;
import taxi.model.Ride;

import java.util.Objects;

public final class SurgePricing implements PricingStrategy {
    private final DemandSource source;
    private final StandardPricing base = new StandardPricing();

    public SurgePricing(DemandSource source) { this.source = Objects.requireNonNull(source); }

    @Override public long price(Ride ride) {
        double multiplier = Math.max(1.0, Math.min(3.0, source.current().ratio()));
        return Math.round(base.price(ride) * multiplier);
    }
}
