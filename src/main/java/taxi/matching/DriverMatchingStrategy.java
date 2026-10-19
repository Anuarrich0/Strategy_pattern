package taxi.matching;

import taxi.model.Driver;

import java.util.List;

public interface DriverMatchingStrategy {
    Driver match(List<Driver> availableDrivers);
}
