package taxi;

import java.util.Locale;
import java.util.Objects;

public final class PricingSelector {
    public record Choice(String name, DemandSource source) {}
    private final DemandSource source;

    public PricingSelector(DemandSource source) { this.source = Objects.requireNonNull(source); }

    public Choice manual(String name) {
        return switch (name.toLowerCase(Locale.ROOT)) {
            case "standard", "surge", "airport" -> new Choice(name.toLowerCase(Locale.ROOT), source);
            default -> throw new IllegalArgumentException("Unknown pricing strategy: " + name);
        };
    }

    public Choice select(Ride ride) {
        if (ride.airport()) return manual("airport");
        if (source.current().ratio() >= 1.5) return manual("surge");
        return manual("standard");
    }
}
