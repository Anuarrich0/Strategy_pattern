package taxi.pricing;

import taxi.model.Ride;

public final class AirportPricing implements PricingStrategy {
    @Override public long price(Ride ride) {
        return 4500 + Math.round(Math.max(0, ride.distanceKm() - 20) * 200);
    }
}
