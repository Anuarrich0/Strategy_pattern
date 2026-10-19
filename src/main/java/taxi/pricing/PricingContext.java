package taxi.pricing;

import taxi.model.Ride;

import java.util.Objects;

public final class PricingContext {
    private PricingStrategy strategy;

    public PricingContext(PricingStrategy strategy) { setStrategy(strategy); }
    public void setStrategy(PricingStrategy strategy) { this.strategy = Objects.requireNonNull(strategy); }
    public long price(Ride ride) { return strategy.price(Objects.requireNonNull(ride)); }
}
