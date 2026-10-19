package taxi;

import java.util.Locale;
import java.util.Objects;

public final class PricingSelector {
    private final DemandSource source;

    public PricingSelector(DemandSource source) { this.source = Objects.requireNonNull(source); }

    public PricingStrategy manual(String name) {
        return switch (name.toLowerCase(Locale.ROOT)) {
            case "standard" -> new StandardPricing();
            case "surge" -> new SurgePricing(source);
            case "airport" -> new AirportPricing();
            default -> throw new IllegalArgumentException("Unknown pricing strategy: " + name);
        };
    }

    public PricingStrategy select(Ride ride) {
        if (ride.airport()) return manual("airport");
        if (source.current().ratio() >= 1.5) return manual("surge");
        return manual("standard");
    }
}
