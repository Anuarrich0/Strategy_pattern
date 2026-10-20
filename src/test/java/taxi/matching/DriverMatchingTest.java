package taxi.matching;

import taxi.model.Driver;
import taxi.model.Ride;

import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import static org.junit.jupiter.api.Assertions.*;

class DriverMatchingTest {
    private final Driver near = new Driver("Arman", 1, 4.6, 5);
    private final Driver rated = new Driver("Dias", 3, 4.9, 12);
    private final Driver idle = new Driver("Miras", 2, 4.7, 40);
    private final List<Driver> drivers = List.of(near, rated, idle);
    private final DriverSelector selector = new DriverSelector();

    static Stream<DriverMatchingStrategy> strategies() {
        return Stream.of(new NearestDriver(), new HighestRatedDriver(), new LongestIdleDriver());
    }

    @ParameterizedTest
    @CsvSource({"nearest,Arman", "rated,Dias", "idle,Miras"})
    void eachStrategyChoosesItsExpectedDriver(String name, String expected) {
        assertEquals(expected, new DriverMatchingContext(selector.manual(name)).match(drivers).name());
    }

    @ParameterizedTest
    @CsvSource({"false,false,Arman", "true,false,Miras", "false,true,Dias", "true,true,Dias"})
    void selectorCoversEveryRuleWithPremiumTakingPrecedence(boolean airport, boolean premium, String expected) {
        Ride ride = new Ride(10, 15, airport, premium);
        assertEquals(expected, new DriverMatchingContext(selector.select(ride)).match(drivers).name());
    }

    @ParameterizedTest
    @MethodSource("strategies")
    void everyStrategyReturnsAnAvailableDriverWithoutChangingInput(DriverMatchingStrategy strategy) {
        assertTrue(drivers.contains(new DriverMatchingContext(strategy).match(drivers)));
        assertEquals(List.of(near, rated, idle), drivers);
    }

    @ParameterizedTest
    @MethodSource("strategies")
    void everyStrategyRejectsAnEmptyPool(DriverMatchingStrategy strategy) {
        assertThrows(IllegalArgumentException.class, () -> new DriverMatchingContext(strategy).match(List.of()));
    }

    @ParameterizedTest
    @MethodSource("strategies")
    void tiesUseAlphabeticalNameRegardlessOfInputOrder(DriverMatchingStrategy strategy) {
        Driver a = new Driver("A", 1, 5, 10);
        Driver b = new Driver("B", 1, 5, 10);
        assertEquals(a, strategy.match(List.of(b, a)));
        assertEquals(a, strategy.match(List.of(a, b)));
    }

    @Test void contextSwitchesStrategyDuringTheSameRun() {
        DriverMatchingContext context = new DriverMatchingContext(new NearestDriver());
        assertEquals(near, context.match(drivers));
        context.setStrategy(new HighestRatedDriver());
        assertEquals(rated, context.match(drivers));
        context.setStrategy(new LongestIdleDriver());
        assertEquals(idle, context.match(drivers));
    }

    @Test void rejectsInvalidDrivers() {
        assertThrows(IllegalArgumentException.class, () -> new Driver(" ", 1, 4, 1));
        assertThrows(IllegalArgumentException.class, () -> new Driver(null, 1, 4, 1));
        for (double distance : new double[]{-1, Double.NaN, Double.POSITIVE_INFINITY, 10_001})
            assertThrows(IllegalArgumentException.class, () -> new Driver("A", distance, 4, 1));
        for (double rating : new double[]{-1, 6, Double.NaN, Double.POSITIVE_INFINITY})
            assertThrows(IllegalArgumentException.class, () -> new Driver("A", 1, rating, 1));
        assertThrows(IllegalArgumentException.class, () -> new Driver("A", 1, 4, -1));
        assertThrows(IllegalArgumentException.class, () -> new Driver("A", 1, 4, 1_000_001));
    }

    @Test void rejectsUnknownChoiceAndNullInputs() {
        assertThrows(IllegalArgumentException.class, () -> selector.manual("unknown"));
        assertThrows(NullPointerException.class, () -> new DriverMatchingContext(null));
        DriverMatchingContext context = new DriverMatchingContext(new NearestDriver());
        assertThrows(NullPointerException.class, () -> context.setStrategy(null));
        assertThrows(NullPointerException.class, () -> context.match(null));
        assertThrows(NullPointerException.class, () -> context.match(java.util.Arrays.asList(near, null)));
    }
}
