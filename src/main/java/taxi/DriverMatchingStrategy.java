package taxi;

import java.util.List;

public interface DriverMatchingStrategy {
    Driver match(List<Driver> availableDrivers);
}
