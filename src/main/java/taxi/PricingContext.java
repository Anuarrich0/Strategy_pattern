package taxi;

import java.util.Objects;

// Before refactoring: every fare algorithm is coupled to this type switch.
public final class PricingContext {
    private PricingSelector.Choice choice;

    public PricingContext(PricingSelector.Choice choice) { setStrategy(choice); }
    public void setStrategy(PricingSelector.Choice choice) { this.choice = Objects.requireNonNull(choice); }

    public long price(Ride ride) {
        return switch (choice.name()) {
            case "standard" -> 500 + Math.round(ride.distanceKm() * 120) + ride.durationMinutes() * 20L;
            case "surge" -> Math.round((500 + Math.round(ride.distanceKm() * 120) + ride.durationMinutes() * 20L)
                    * Math.max(1.0, Math.min(3.0, choice.source().current().ratio())));
            case "airport" -> 4500 + Math.round(Math.max(0, ride.distanceKm() - 20) * 200);
            default -> throw new IllegalArgumentException("Unknown pricing strategy");
        };
    }
}
