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
        return output.toString(StandardCharsets.UTF_8);
    }

    @Test void defaultDemoShowsEveryAlgorithmAndAutomaticChoice() {
        String output = demo("--demo");
        for (String expected : new String[]{"Обычный: 2000 KZT", "По спросу: 4000 KZT", "Аэропортовый: 4500 KZT",
                "Ближайший: Aruzhan", "Лучший рейтинг: Dias", "Дольше всех ждёт: Miras",
                "Автоматически: 4000 KZT, водитель Aruzhan"}) assertTrue(output.contains(expected), expected);
    }

    @Test void customAirportPremiumInputActivatesBothPriorityRules() {
        String output = demo("25", "40", "true", "true", "100", "10");
        assertTrue(output.contains("Автоматически: 5500 KZT, водитель Dias"));
    }

    @Test void lowDemandAndNonPremiumAirportActivateOtherRules() {
        assertTrue(demo("10", "15", "false", "false", "10", "10").contains("Автоматически: 2000 KZT, водитель Aruzhan"));
        assertTrue(demo("10", "15", "true", "false", "10", "10").contains("Автоматически: 4500 KZT, водитель Miras"));
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
        String output = console("2\n2\n1\n2\n1\n3\n0\n0\n");
        assertTrue(output.contains("Стоимость: 4300 KZT"));
        assertTrue(output.contains("Водитель: Dias"));
        assertTrue(output.contains("Стоимость: 5500 KZT"));
        assertTrue(output.contains("Водитель: Miras"));
        assertTrue(output.indexOf("Стоимость: 4300 KZT") < output.indexOf("Стоимость: 5500 KZT"));
        assertTrue(output.contains("Обычный: 4300 KZT"));
        assertTrue(output.contains("По спросу: 8600 KZT"));
        assertTrue(output.contains("До свидания!"));
    }

    @Test void twoRoutesWorkWithoutAskingForExtraTripData() {
        String output = console("1\n1\n0\n2\n1\n0\n0\n");
        assertTrue(output.contains("Поездка по городу, 10 км."));
        assertTrue(output.contains("Стоимость: 4000 KZT"));
        assertTrue(output.contains("Водитель: Aruzhan"));
        assertTrue(output.contains("Поездка в аэропорт, 25 км."));
        assertTrue(output.contains("Стоимость: 5500 KZT"));
        assertTrue(output.contains("Водитель: Miras"));
        for (String removed : new String[]{"Время поездки", "Клиент:", "Симуляция", "Расстояние в км", "Driver[", "Pricing"})
            assertFalse(output.contains(removed), output);
    }

    @Test void manualChoiceCanOverrideAutomaticPolicies() {
        String output = console("1\n2\n3\n2\n1\n0\n0\n");
        assertTrue(output.contains("Стоимость: 4500 KZT"));
        assertTrue(output.contains("Водитель: Dias"));
        assertTrue(output.contains("Стоимость: 4000 KZT"));
        assertTrue(output.contains("Водитель: Aruzhan"));
    }

    @Test void invalidChoicesAreRetriedWithoutDroppingTheTrip() {
        String output = console("wrong\n9\n1\n9\n2\n0\n2\n4\n3\n1\n0\n0\n");
        assertTrue(output.contains("Введите номер"));
        assertTrue(output.contains("Стоимость: 4000 KZT"));
        assertTrue(output.contains("Водитель: Miras"));
        assertTrue(output.contains("Водитель: Aruzhan"));
        assertTrue(output.contains("До свидания!"));
    }

    @Test void explicitExitAndClosedInputFinishCleanly() {
        assertTrue(console("0\n").contains("До свидания!"));
        assertTrue(console("").contains("Ввод завершён"));
        assertTrue(console("1\n").contains("Ввод завершён"));
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
            assertTrue(output.contains("ТАКСИ - ИНТЕРАКТИВНОЕ МЕНЮ"), output);
            assertTrue(output.contains("До свидания!"), output);
        } finally {
            process.destroyForcibly();
        }
    }
}
