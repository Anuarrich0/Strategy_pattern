package taxi;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MainTest {
    private String demo(String... args) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Main.run(args, new PrintStream(output, true, StandardCharsets.UTF_8));
        return output.toString(StandardCharsets.UTF_8);
    }

    @Test void defaultDemoShowsEveryAlgorithmAndAutomaticChoice() {
        String output = demo("--demo");
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

    private String console(String input) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Main.interactive(new Scanner(input), new PrintStream(output, true, StandardCharsets.UTF_8));
        return output.toString(StandardCharsets.UTF_8);
    }

    @Test void interactiveTripCanSwitchFromManualToAutomaticAndCompareAll() {
        String output = console("1\n2\n25\n40\n2\n2\n2\n1\n3\n1\n3\n0\n0\n");
        assertTrue(output.contains("Стоимость: 4300 KZT"));
        assertTrue(output.contains("Водитель: Miras"));
        assertTrue(output.contains("Стоимость: 5500 KZT"));
        assertTrue(output.contains("Водитель: Dias"));
        assertTrue(output.indexOf("Стоимость: 4300 KZT") < output.indexOf("Стоимость: 5500 KZT"));
        assertTrue(output.contains("standard -> 4300 KZT"));
        assertTrue(output.contains("surge -> 8600 KZT"));
        assertTrue(output.contains("До свидания!"));
    }

    @Test void acceptsDecimalCommaAndCanStartAnotherTrip() {
        String output = console("1\n1\n10,5\n15\n1\n1\n1\n0\n1\n2\n25\n40\n1\n2\n1\n0\n0\n");
        assertTrue(output.contains("Стоимость: 2060 KZT"));
        assertTrue(output.contains("Подбор: NearestDriver"));
        assertTrue(output.contains("Стоимость: 5500 KZT"));
        assertTrue(output.contains("Подбор: LongestIdleDriver"));
    }

    @Test void manualAirportFareAndNearestDriverCanOverrideAutomaticPolicies() {
        String output = console("1\n1\n10\n15\n2\n2\n2\n3\n1\n1\n0\n0\n");
        assertTrue(output.contains("Тариф: AirportPricing"));
        assertTrue(output.contains("Стоимость: 4500 KZT"));
        assertTrue(output.contains("Водитель: Aruzhan"));
        assertTrue(output.contains("Тариф: SurgePricing"));
        assertTrue(output.contains("Стоимость: 4000 KZT"));
        assertTrue(output.contains("Водитель: Dias"));
    }

    @Test void invalidInputIsRetriedWithoutDroppingTheTrip() {
        String output = console("wrong\n9\n1\n3\n1\nNaN\n-1\n10\n-1\n15\n3\n1\n0\n2\n9\n2\n0\n2\n4\n2\n1\n0\n0\n");
        assertTrue(output.contains("Ошибка:"));
        assertTrue(output.contains("Стоимость: 4000 KZT"));
        assertTrue(output.contains("Водитель: Aruzhan"));
        assertTrue(output.contains("До свидания!"));
    }

    @Test void explicitExitAndClosedInputFinishCleanly() {
        assertTrue(console("0\n").contains("До свидания!"));
        assertTrue(console("").contains("Ввод завершён"));
        assertTrue(console("1\n2\n").contains("Ввод завершён"));
    }
}
