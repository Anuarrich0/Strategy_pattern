package taxi;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MainTest {
    private String demo(String... args) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Main.run(args, new PrintStream(output, true, StandardCharsets.UTF_8));
        return output.toString(StandardCharsets.UTF_8);
    }

    @Test void defaultDemoShowsEveryAlgorithmAndAutomaticChoice() {
        String output = demo();
        for (String expected : new String[]{"standard -> 2000 KZT", "surge -> 4000 KZT", "airport -> 4500 KZT",
                "nearest -> Aruzhan", "rated -> Dias", "idle -> Miras", "SurgePricing -> 4000 KZT",
                "NearestDriver -> Aruzhan", "RUNTIME SWITCHING"}) assertTrue(output.contains(expected), expected);
    }

    @Test void customAirportPremiumInputActivatesBothPriorityRules() {
        String output = demo("25", "40", "true", "true", "100", "10");
        assertTrue(output.contains("AirportPricing -> 5500 KZT"));
        assertTrue(output.contains("HighestRatedDriver -> Dias"));
    }

    @Test void lowDemandAndNonPremiumAirportActivateOtherRules() {
        assertTrue(demo("10", "15", "false", "false", "10", "10").contains("StandardPricing -> 2000 KZT"));
        assertTrue(demo("10", "15", "true", "false", "10", "10").contains("LongestIdleDriver -> Miras"));
    }

    @Test void malformedArgumentsFailClearly() {
        assertThrows(IllegalArgumentException.class, () -> demo("1"));
        assertThrows(IllegalArgumentException.class, () -> demo("x", "15", "false", "false", "10", "10"));
        assertThrows(IllegalArgumentException.class, () -> demo("10", "15", "yes", "false", "10", "10"));
        assertThrows(IllegalArgumentException.class, () -> demo("10", "15", "false", "false", "10", "0"));
    }
}
