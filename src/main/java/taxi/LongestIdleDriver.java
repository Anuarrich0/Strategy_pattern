package taxi;

import java.util.Comparator;
import java.util.List;

public final class LongestIdleDriver implements DriverMatchingStrategy {
    @Override public Driver match(List<Driver> availableDrivers) {
        return availableDrivers.stream()
                .min(Comparator.comparingInt(Driver::idleMinutes).reversed().thenComparing(Driver::name))
                .orElseThrow(() -> new IllegalArgumentException("No available drivers"));
    }
}
