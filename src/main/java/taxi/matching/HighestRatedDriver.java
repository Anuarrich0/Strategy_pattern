package taxi.matching;

import taxi.model.Driver;

import java.util.Comparator;
import java.util.List;

public final class HighestRatedDriver implements DriverMatchingStrategy {
    @Override public Driver match(List<Driver> availableDrivers) {
        return availableDrivers.stream()
                .min(Comparator.comparingDouble(Driver::rating).reversed().thenComparing(Driver::name))
                .orElseThrow(() -> new IllegalArgumentException("No available drivers"));
    }
}
