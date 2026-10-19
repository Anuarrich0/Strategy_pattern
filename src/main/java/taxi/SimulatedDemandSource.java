package taxi;

import java.util.Objects;

public record SimulatedDemandSource(Demand demand) implements DemandSource {
    public SimulatedDemandSource { Objects.requireNonNull(demand); }
    @Override public Demand current() { return demand; }
}
