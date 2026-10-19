package taxi;

import taxi.demand.Demand;
import taxi.model.Driver;
import taxi.matching.DriverMatchingContext;
import taxi.pricing.PricingContext;
import taxi.model.Ride;
import taxi.demand.SimulatedDemandSource;
import taxi.pricing.StandardPricing;
import taxi.pricing.SurgePricing;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class StrategyExtensionTest {
    @Test void newAlgorithmsPlugIntoExistingContextsWithoutEditingThem() {
        Ride ride = new Ride(10, 15, false, false);
        PricingContext pricing = new PricingContext(input -> 777);
        assertEquals(777, pricing.price(ride));
        Driver a = new Driver("A", 1, 4, 1);
        Driver b = new Driver("B", 2, 5, 2);
        DriverMatchingContext matching = new DriverMatchingContext(pool -> pool.get(pool.size() - 1));
        assertEquals(b, matching.match(List.of(a, b)));
    }

    @Test void oneSurgeStrategyReadsNewExternalValuesOnEachCall() {
        AtomicReference<Demand> fixedValues = new AtomicReference<>(new Demand(10, 10));
        SurgePricing strategy = new SurgePricing(fixedValues::get);
        Ride ride = new Ride(10, 15, false, false);
        assertEquals(2000, strategy.price(ride));
        fixedValues.set(new Demand(25, 10));
        assertEquals(5000, strategy.price(ride));
    }

    @Test void failedExternalSourceIsNotSilentlyReplacedWithAnInventedFare() {
        SurgePricing strategy = new SurgePricing(() -> { throw new IllegalStateException("Unavailable"); });
        assertThrows(IllegalStateException.class, () -> strategy.price(new Ride(1, 1, false, false)));
    }

    @Test void rejectsNullStrategyReplacementAndDemandSource() {
        assertThrows(NullPointerException.class, () -> new SurgePricing(null));
        assertThrows(NullPointerException.class, () -> new SimulatedDemandSource(null));
        PricingContext context = new PricingContext(new StandardPricing());
        assertThrows(NullPointerException.class, () -> context.setStrategy(null));
        assertThrows(NullPointerException.class, () -> context.price(null));
    }
}
