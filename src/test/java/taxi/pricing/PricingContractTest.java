package taxi.pricing;

import taxi.demand.Demand;
import taxi.demand.DemandSource;
import taxi.model.Ride;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import static org.junit.jupiter.api.Assertions.*;

class PricingContractTest {
    // Fake external source: tests never depend on a live service or clock.
    private final DemandSource fake = () -> new Demand(20, 10);
    private final PricingSelector selector = new PricingSelector(fake);
    private final Ride ride = new Ride(10, 15, false, false);

    @ParameterizedTest
    @CsvSource({"standard,2000", "surge,4000", "airport,4500"})
    void eachFareHasItsOwnExpectedResult(String strategy, long expected) {
        assertEquals(expected, new PricingContext(selector.manual(strategy)).price(ride));
    }

    @ParameterizedTest
    @ValueSource(strings = {"standard", "surge", "airport"})
    void everyFareIsNonNegative(String strategy) {
        PricingContext context = new PricingContext(selector.manual(strategy));
        for (Ride input : new Ride[]{new Ride(0, 0, false, false), ride,
                new Ride(20, 30, true, true), new Ride(25.5, 40, true, false),
                new Ride(10_000, 10_000, false, false)}) {
            assertTrue(context.price(input) >= 0);
        }
    }

    @Test void airportIncludesTwentyKmThenChargesOnlyTheExcess() {
        PricingContext context = new PricingContext(selector.manual("airport"));
        assertEquals(4500, context.price(new Ride(20, 90, true, false)));
        assertEquals(5600, context.price(new Ride(25.5, 90, true, false)));
    }

    @ParameterizedTest
    @CsvSource({"0,10,2000", "10,10,2000", "15,10,3000", "20,10,4000", "100,10,6000"})
    void surgeUsesLiveDemandWithAFloorAndCap(int requests, int drivers, long expected) {
        PricingSelector withDemand = new PricingSelector(() -> new Demand(requests, drivers));
        assertEquals(expected, new PricingContext(withDemand.manual("surge")).price(ride));
    }

    @ParameterizedTest
    @CsvSource({"false,14,2000", "false,15,3000", "true,14,4500", "true,100,4500"})
    void automaticSelectionCoversEveryRuleAndAirportPrecedence(boolean airport, int requests, long expected) {
        PricingSelector automatic = new PricingSelector(() -> new Demand(requests, 10));
        Ride input = new Ride(10, 15, airport, false);
        assertEquals(expected, new PricingContext(automatic.select(input)).price(input));
    }

    @Test void contextCanChangeStrategyDuringTheSameRun() {
        PricingContext context = new PricingContext(selector.manual("standard"));
        assertEquals(2000, context.price(ride));
        context.setStrategy(selector.manual("surge"));
        assertEquals(4000, context.price(ride));
        context.setStrategy(selector.manual("airport"));
        assertEquals(4500, context.price(ride));
    }

    @Test void rejectsUnknownManualChoiceAndNullDependencies() {
        assertThrows(IllegalArgumentException.class, () -> selector.manual("unknown"));
        assertThrows(NullPointerException.class, () -> new PricingSelector(null));
        assertThrows(NullPointerException.class, () -> new PricingContext(null));
    }

    @Test void rejectsInvalidRideAndDemand() {
        for (double distance : new double[]{-1, Double.NaN, Double.POSITIVE_INFINITY, 10_001})
            assertThrows(IllegalArgumentException.class, () -> new Ride(distance, 1, false, false));
        assertThrows(IllegalArgumentException.class, () -> new Ride(1, -1, false, false));
        assertThrows(IllegalArgumentException.class, () -> new Ride(1, 10_001, false, false));
        assertThrows(IllegalArgumentException.class, () -> new Demand(-1, 10));
        assertThrows(IllegalArgumentException.class, () -> new Demand(10, 0));
        assertThrows(IllegalArgumentException.class, () -> new Demand(1_000_001, 10));
        assertThrows(IllegalArgumentException.class, () -> new Demand(1, 1_000_001));
    }
}
