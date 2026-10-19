package taxi;

import java.util.Locale;

public final class DriverSelector {
    public DriverMatchingStrategy select(Ride ride) {
        if (ride.premium()) return new HighestRatedDriver();
        if (ride.airport()) return new LongestIdleDriver();
        return new NearestDriver();
    }

    public DriverMatchingStrategy manual(String name) {
        return switch (name.toLowerCase(Locale.ROOT)) {
            case "nearest" -> new NearestDriver();
            case "rated" -> new HighestRatedDriver();
            case "idle" -> new LongestIdleDriver();
            default -> throw new IllegalArgumentException("Unknown driver strategy: " + name);
        };
    }
}
