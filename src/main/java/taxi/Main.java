package taxi;

import java.io.PrintStream;
import java.util.List;
import java.util.Locale;

public final class Main {
    private Main() {}

    public static void main(String[] args) {
        try {
            run(args, System.out);
        } catch (IllegalArgumentException error) {
            System.err.println("Error: " + error.getMessage());
            System.err.println("Usage: java -jar target/taxi-strategy-1.0.0.jar [km minutes airport premium requests drivers]");
            System.err.println("Example: java -jar target/taxi-strategy-1.0.0.jar 10 15 false false 20 10");
            System.exit(1);
        }
    }

    static void run(String[] args, PrintStream out) {
        if (args.length != 0 && args.length != 6)
            throw new IllegalArgumentException("Supply no arguments or exactly six arguments");
        Ride ride = args.length == 0 ? new Ride(10, 15, false, false)
                : new Ride(Double.parseDouble(args[0]), Integer.parseInt(args[1]), bool(args[2]), bool(args[3]));
        Demand demand = args.length == 0 ? new Demand(20, 10)
                : new Demand(Integer.parseInt(args[4]), Integer.parseInt(args[5]));
        DemandSource source = new SimulatedDemandSource(demand);
        PricingSelector pricing = new PricingSelector(source);
        DriverSelector matching = new DriverSelector();
        List<Driver> drivers = List.of(new Driver("Aruzhan", 1, 4.6, 5),
                new Driver("Dias", 3, 4.9, 12), new Driver("Miras", 2, 4.7, 40));

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

    private static boolean bool(String value) {
        return switch (value.toLowerCase(Locale.ROOT)) {
            case "true" -> true;
            case "false" -> false;
            default -> throw new IllegalArgumentException("Boolean values must be true or false");
        };
    }
}
