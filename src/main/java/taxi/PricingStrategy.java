package taxi;

public interface PricingStrategy {
    long price(Ride ride);
}
