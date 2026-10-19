package taxi.matching;

import taxi.model.Driver;

import java.util.Comparator;
import java.util.List;

public final class NearestDriver implements DriverMatchingStrategy {
    @Override public Driver match(List<Driver> availableDrivers) {
        return availableDrivers.stream()
                .min(Comparator.comparingDouble(Driver::distanceKm).thenComparing(Driver::name))
                .orElseThrow(() -> new IllegalArgumentException("No available drivers"));
    }
}
