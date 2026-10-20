package taxi;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import static org.junit.jupiter.api.Assertions.*;

class MainTest {
    private String demo(String... args) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Main.run(args, new PrintStream(output, true, StandardCharsets.UTF_8));
        String text = output.toString(StandardCharsets.UTF_8);
        assertTrue(text.chars().allMatch(character -> character < 128), text);
        return text;
    }

    @Test void defaultDemoShowsEveryAlgorithmAndAutomaticChoice() {
        String output = demo("--demo");
        for (String expected : new String[]{"Standard: 2000 KZT", "Surge: 4000 KZT", "Airport: 4500 KZT",
                "Nearest: Arman", "Highest rated: Dias", "Longest idle: Miras",
                "Automatic: 4000 KZT, driver Arman"}) assertTrue(output.contains(expected), expected);
    }

    @Test void customAirportPremiumInputActivatesBothPriorityRules() {
        String output = demo("25", "40", "true", "true", "100", "10");
        assertTrue(output.contains("Automatic: 5500 KZT, driver Dias"));
    }

    @Test void lowDemandAndNonPremiumAirportActivateOtherRules() {
        assertTrue(demo("10", "15", "false", "false", "10", "10").contains("Automatic: 2000 KZT, driver Arman"));
        assertTrue(demo("10", "15", "true", "false", "10", "10").contains("Automatic: 4500 KZT, driver Miras"));
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
        String text = output.toString(StandardCharsets.UTF_8);
        assertTrue(text.chars().allMatch(character -> character < 128), text);
        return text;
    }

    @Test void interactiveTripCanSwitchFromManualToAutomaticAndCompareAll() {
        String output = console("2\n2\n1\n2\n1\n3\n0\n0\n");
        assertTrue(output.contains("Price: 4300 KZT"));
        assertTrue(output.contains("Driver: Dias"));
        assertTrue(output.contains("Price: 5500 KZT"));
        assertTrue(output.contains("Driver: Miras"));
        assertTrue(output.indexOf("Price: 4300 KZT") < output.indexOf("Price: 5500 KZT"));
        assertTrue(output.contains("Standard: 4300 KZT"));
        assertTrue(output.contains("Surge: 8600 KZT"));
        assertTrue(output.contains("Goodbye!"));
    }

    @Test void twoRoutesWorkWithoutAskingForExtraTripData() {
        String output = console("1\n1\n0\n2\n1\n0\n0\n");
        assertTrue(output.contains("City trip, 10 km."));
        assertTrue(output.contains("Price: 4000 KZT"));
        assertTrue(output.contains("Driver: Arman"));
        assertTrue(output.contains("Airport trip, 25 km."));
        assertTrue(output.contains("Price: 5500 KZT"));
        assertTrue(output.contains("Driver: Miras"));
        for (String removed : new String[]{"Trip duration", "Customer:", "Simulation", "Distance in km", "Driver[", "Pricing"})
            assertFalse(output.contains(removed), output);
    }

    @Test void manualChoiceCanOverrideAutomaticPolicies() {
        String output = console("1\n2\n3\n2\n1\n0\n0\n");
        assertTrue(output.contains("Price: 4500 KZT"));
        assertTrue(output.contains("Driver: Dias"));
        assertTrue(output.contains("Price: 4000 KZT"));
        assertTrue(output.contains("Driver: Arman"));
    }

    @Test void invalidChoicesAreRetriedWithoutDroppingTheTrip() {
        String output = console("wrong\n9\n1\n9\n2\n0\n2\n4\n3\n1\n0\n0\n");
        assertTrue(output.contains("Enter a number"));
        assertTrue(output.contains("Price: 4000 KZT"));
        assertTrue(output.contains("Driver: Miras"));
        assertTrue(output.contains("Driver: Arman"));
        assertTrue(output.contains("Goodbye!"));
    }

    @Test void explicitExitAndClosedInputFinishCleanly() {
        assertTrue(console("0\n").contains("Goodbye!"));
        assertTrue(console("").contains("Input closed"));
        assertTrue(console("1\n").contains("Input closed"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"UTF-8", "IBM866"})
    void realMainUsesTheRuntimeOutputEncoding(String encoding) throws Exception {
        String java = ProcessHandle.current().info().command().orElseThrow();
        String classes = Path.of(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI()).toString();
        Process process = new ProcessBuilder(java, "-Dstdout.encoding=" + encoding,
                "-Dsun.stdout.encoding=" + encoding, "-cp", classes, "taxi.Main")
                .redirectErrorStream(true).start();
        try {
            process.getOutputStream().write("0\n".getBytes(StandardCharsets.US_ASCII));
            process.getOutputStream().close();
            assertTrue(process.waitFor(15, TimeUnit.SECONDS), "Menu did not exit");
            String output = new String(process.getInputStream().readAllBytes(), Charset.forName(encoding));
            assertEquals(0, process.exitValue(), output);
            assertTrue(output.contains("TAXI - INTERACTIVE MENU"), output);
            assertTrue(output.contains("Goodbye!"), output);
        } finally {
            process.destroyForcibly();
        }
    }
}
