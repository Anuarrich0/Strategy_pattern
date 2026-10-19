package taxi.matching;

import taxi.model.Driver;

import java.util.List;
import java.util.Objects;

public final class DriverMatchingContext {
    private DriverMatchingStrategy strategy;

    public DriverMatchingContext(DriverMatchingStrategy strategy) { setStrategy(strategy); }
    public void setStrategy(DriverMatchingStrategy strategy) { this.strategy = Objects.requireNonNull(strategy); }
    public Driver match(List<Driver> drivers) { return strategy.match(List.copyOf(drivers)); }
}
