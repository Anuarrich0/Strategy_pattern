package taxi.pricing;

import taxi.model.Ride;

public final class StandardPricing implements PricingStrategy {
    @Override public long price(Ride ride) {
        return 500 + Math.round(ride.distanceKm() * 120) + ride.durationMinutes() * 20L;
    }
}
