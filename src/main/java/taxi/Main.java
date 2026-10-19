package taxi;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.function.Function;

public final class Main {
    private Main() {}

    public static void main(String[] args) {
        PrintStream out = new PrintStream(System.out, true, StandardCharsets.UTF_8);
        try {
            if (args.length == 0) interactive(new Scanner(System.in, StandardCharsets.UTF_8), out);
            else run(args, out);
        } catch (IllegalArgumentException error) {
            System.err.println("Error: " + error.getMessage());
            System.err.println("Usage: java -jar target/taxi-strategy-1.0.0.jar [--demo | km minutes airport premium requests drivers]");
            System.err.println("Example: java -jar target/taxi-strategy-1.0.0.jar 10 15 false false 20 10");
            System.exit(1);
        }
    }

    static void run(String[] args, PrintStream out) {
        if (args.length == 1 && args[0].equals("--demo")) args = new String[0];
        if (args.length != 0 && args.length != 6)
            throw new IllegalArgumentException("Supply no arguments or exactly six arguments");
        Ride ride = args.length == 0 ? new Ride(10, 15, false, false)
                : new Ride(Double.parseDouble(args[0]), Integer.parseInt(args[1]), bool(args[2]), bool(args[3]));
        Demand demand = args.length == 0 ? new Demand(20, 10)
                : new Demand(Integer.parseInt(args[4]), Integer.parseInt(args[5]));
        demonstrate(ride, demand, out);
    }

    private static List<Driver> availableDrivers() {
        return List.of(new Driver("Aruzhan", 1, 4.6, 5),
                new Driver("Dias", 3, 4.9, 12), new Driver("Miras", 2, 4.7, 40));
    }

    private static void demonstrate(Ride ride, Demand demand, PrintStream out) {
        DemandSource source = new SimulatedDemandSource(demand);
        PricingSelector pricing = new PricingSelector(source);
        DriverSelector matching = new DriverSelector();
        List<Driver> drivers = availableDrivers();

        out.println("TAXI STRATEGY DEMO");
        out.println("Same ride: " + ride);
        out.println("Simulated external demand: " + demand);
        out.println("Same available drivers: " + drivers);
        out.println("\nMANUAL PRICING / RUNTIME SWITCHING (one context)");
        PricingContext fare = new PricingContext(pricing.manual("standard"));
        for (String name : List.of("standard", "surge", "airport")) {
            fare.setStrategy(pricing.manual(name));
            out.println(name + " -> " + fare.price(ride) + " KZT");
        }
        out.println("\nAUTOMATIC PRICING");
        PricingStrategy selectedFare = pricing.select(ride);
        fare.setStrategy(selectedFare);
        out.println(selectedFare.getClass().getSimpleName() + " -> " + fare.price(ride) + " KZT");

        out.println("\nMANUAL DRIVER MATCHING / RUNTIME SWITCHING (one context)");
        DriverMatchingContext driver = new DriverMatchingContext(matching.manual("nearest"));
        for (String name : List.of("nearest", "rated", "idle")) {
            driver.setStrategy(matching.manual(name));
            out.println(name + " -> " + driver.match(drivers).name());
        }
        out.println("\nAUTOMATIC DRIVER MATCHING");
        DriverMatchingStrategy selectedDriver = matching.select(ride);
        driver.setStrategy(selectedDriver);
        out.println(selectedDriver.getClass().getSimpleName() + " -> " + driver.match(drivers).name());
    }

    static void interactive(Scanner input, PrintStream out) {
        out.println("ТАКСИ — ИНТЕРАКТИВНОЕ МЕНЮ");
        try {
            while (true) {
                int start = integer(input, out, "\n1 — Новая поездка; 0 — Выход: ", 0, 1);
                if (start == 0) {
                    out.println("До свидания!");
                    return;
                }
                boolean airport = integer(input, out, "Куда едем? 1 — Город; 2 — Аэропорт: ", 1, 2) == 2;
                double km = ask(input, out, "Расстояние в км (0–10000): ", value -> {
                    double distance = Double.parseDouble(value.replace(',', '.'));
                    new Ride(distance, 0, false, false);
                    return distance;
                });
                int minutes = integer(input, out, "Время поездки в минутах (0–10000): ", 0, 10_000);
                boolean premium = integer(input, out, "Клиент: 1 — Обычный; 2 — Премиум: ", 1, 2) == 2;
                Ride ride = new Ride(km, minutes, airport, premium);
                int demandMode = integer(input, out,
                        "Симуляция внешнего спроса: 1 — Обычный (10/10); 2 — Высокий (20/10): ", 1, 2);
                Demand demand = new Demand(demandMode * 10, 10);
                PricingSelector pricing = new PricingSelector(new SimulatedDemandSource(demand));
                DriverSelector matching = new DriverSelector();
                PricingContext fare = new PricingContext(pricing.manual("standard"));
                DriverMatchingContext driver = new DriverMatchingContext(matching.manual("nearest"));
                List<Driver> drivers = availableDrivers();
                out.println("\nПоездка: " + (airport ? "аэропорт" : "город") + ", " + km + " км, " + minutes + " мин.");
                out.println("Доступные водители: " + drivers);
                out.println("Меняйте режим для этой же поездки — объекты контекстов сохраняются.");

                while (true) {
                    int action = integer(input, out,
                            "\n1 — Автоматический выбор; 2 — Ручной выбор; 3 — Сравнить все; 0 — Назад: ", 0, 3);
                    if (action == 0) break;
                    if (action == 3) {
                        demonstrate(ride, demand, out);
                        continue;
                    }
                    PricingStrategy selectedFare;
                    DriverMatchingStrategy selectedDriver;
                    if (action == 1) {
                        selectedFare = pricing.select(ride);
                        selectedDriver = matching.select(ride);
                    } else {
                        int tariff = integer(input, out,
                                "Тариф: 1 — Обычный; 2 — По спросу; 3 — Аэропортовый: ", 1, 3);
                        int policy = integer(input, out,
                                "Водитель: 1 — Ближайший; 2 — Лучший рейтинг; 3 — Дольше всех ждёт: ", 1, 3);
                        selectedFare = pricing.manual(List.of("standard", "surge", "airport").get(tariff - 1));
                        selectedDriver = matching.manual(List.of("nearest", "rated", "idle").get(policy - 1));
                    }
                    fare.setStrategy(selectedFare);
                    driver.setStrategy(selectedDriver);
                    out.println("Тариф: " + selectedFare.getClass().getSimpleName());
                    out.println("Стоимость: " + fare.price(ride) + " KZT");
                    out.println("Подбор: " + selectedDriver.getClass().getSimpleName());
                    out.println("Водитель: " + driver.match(drivers).name());
                }
            }
        } catch (NoSuchElementException endOfInput) {
            out.println("\nВвод завершён. До свидания!");
        }
    }

    private static int integer(Scanner input, PrintStream out, String prompt, int min, int max) {
        return ask(input, out, prompt, value -> {
            int number = Integer.parseInt(value);
            if (number < min || number > max)
                throw new IllegalArgumentException("Допустимый диапазон: " + min + "–" + max);
            return number;
        });
    }

    private static <T> T ask(Scanner input, PrintStream out, String prompt, Function<String, T> parse) {
        while (true) {
            out.print(prompt);
            out.flush();
            String value = input.nextLine().strip();
            try {
                return parse.apply(value);
            } catch (IllegalArgumentException invalid) {
                out.println("Ошибка: проверьте формат и допустимый диапазон. Повторите ввод.");
            }
        }
    }

    private static boolean bool(String value) {
        return switch (value.toLowerCase(Locale.ROOT)) {
            case "true" -> true;
            case "false" -> false;
            default -> throw new IllegalArgumentException("Boolean values must be true or false");
        };
    }
}
