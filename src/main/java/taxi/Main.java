package taxi;

import taxi.demand.Demand;
import taxi.model.Driver;
import taxi.matching.DriverMatchingContext;
import taxi.matching.DriverSelector;
import taxi.pricing.PricingContext;
import taxi.pricing.PricingSelector;
import taxi.model.Ride;
import taxi.demand.DemandSource;
import taxi.demand.FileDemandSource;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Scanner;

public final class Main {
    private Main() {}

    public static void main(String[] args) {
        PrintStream out = System.out;
        try {
            if (args.length == 0) interactive(new Scanner(System.in,
                    System.console() == null ? StandardCharsets.UTF_8 : System.console().charset()), out);
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
        DemandSource source;
        if (args.length == 0) source = new FileDemandSource(Path.of("demand.properties"));
        else {
            Demand demand = new Demand(Integer.parseInt(args[4]), Integer.parseInt(args[5]));
            source = () -> demand;
        }
        demonstrate(ride, source, out);
    }

    private static List<Driver> availableDrivers() {
        return List.of(new Driver("Arman", 1, 4.6, 5),
                new Driver("Dias", 3, 4.9, 12), new Driver("Miras", 2, 4.7, 40));
    }

    private static void demonstrate(Ride ride, DemandSource source, PrintStream out) {
        PricingSelector pricing = new PricingSelector(source);
        DriverSelector matching = new DriverSelector();
        List<Driver> drivers = availableDrivers();
        PricingContext fare = new PricingContext(pricing.manual("standard"));
        DriverMatchingContext driver = new DriverMatchingContext(matching.manual("nearest"));
        String[] tariffs = {"standard", "surge", "airport"};
        String[] fareNames = {"Standard", "Surge", "Airport"};
        String[] policies = {"nearest", "rated", "idle"};
        String[] driverNames = {"Nearest", "Highest rated", "Longest idle"};

        out.println("\nComparison for the same trip: " + ride.distanceKm() + " km.");
        out.println("All fares:");
        for (int i = 0; i < tariffs.length; i++) {
            fare.setStrategy(pricing.manual(tariffs[i]));
            out.println(fareNames[i] + ": " + fare.price(ride) + " KZT");
        }
        out.println("All driver selection options:");
        for (int i = 0; i < policies.length; i++) {
            driver.setStrategy(matching.manual(policies[i]));
            out.println(driverNames[i] + ": " + driver.match(drivers).name());
        }
        fare.setStrategy(pricing.select(ride));
        driver.setStrategy(matching.select(ride));
        out.println("Automatic: " + fare.price(ride) + " KZT, driver " + driver.match(drivers).name());
    }

    static void interactive(Scanner input, PrintStream out) {
        out.println("TAXI - INTERACTIVE MENU");
        try {
            while (true) {
                int route = integer(input, out,
                        "\nWhere are we going?\n1 - City (10 km)\n2 - Airport (25 km)\n0 - Exit\nYour choice: ", 0, 2);
                if (route == 0) {
                    out.println("Goodbye!");
                    return;
                }
                Ride ride = route == 1 ? new Ride(10, 15, false, false) : new Ride(25, 40, true, false);
                DemandSource source = new FileDemandSource(Path.of("demand.properties"));
                PricingSelector pricing = new PricingSelector(source);
                DriverSelector matching = new DriverSelector();
                PricingContext fare = new PricingContext(pricing.manual("standard"));
                DriverMatchingContext driver = new DriverMatchingContext(matching.manual("nearest"));
                List<Driver> drivers = availableDrivers();
                out.println(route == 1 ? "\nCity trip, 10 km." : "\nAirport trip, 25 km.");

                while (true) {
                    int action = integer(input, out,
                            "\n1 - Choose automatically\n2 - Choose manually\n3 - Compare all options\n0 - Another trip\nYour choice: ", 0, 3);
                    if (action == 0) break;
                    if (action == 3) {
                        demonstrate(ride, source, out);
                        continue;
                    }
                    if (action == 1) {
                        fare.setStrategy(pricing.select(ride));
                        driver.setStrategy(matching.select(ride));
                    } else {
                        int tariff = integer(input, out,
                                "\nFare: 1 - Standard; 2 - Surge; 3 - Airport: ", 1, 3);
                        int policy = integer(input, out,
                                "Driver: 1 - Nearest; 2 - Highest rated; 3 - Longest idle: ", 1, 3);
                        fare.setStrategy(pricing.manual(List.of("standard", "surge", "airport").get(tariff - 1)));
                        driver.setStrategy(matching.manual(List.of("nearest", "rated", "idle").get(policy - 1)));
                    }
                    out.println("Price: " + fare.price(ride) + " KZT");
                    out.println("Driver: " + driver.match(drivers).name());
                }
            }
        } catch (NoSuchElementException endOfInput) {
            out.println("\nInput closed. Goodbye!");
        }
    }

    private static int integer(Scanner input, PrintStream out, String prompt, int min, int max) {
        while (true) {
            out.print(prompt);
            out.flush();
            String value = input.nextLine().strip();
            try {
                int number = Integer.parseInt(value);
                if (number >= min && number <= max) return number;
            } catch (NumberFormatException invalid) {
            }
            out.println("Enter a number from " + min + " to " + max + ".");
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
