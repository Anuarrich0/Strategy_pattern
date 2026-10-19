package taxi.pricing;

import taxi.model.Ride;

public interface PricingStrategy {
    long price(Ride ride);
}
