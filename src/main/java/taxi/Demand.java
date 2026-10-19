package taxi;

public record Demand(int requests, int availableDrivers) {
    public Demand {
        if (requests < 0 || requests > 1_000_000 || availableDrivers < 1 || availableDrivers > 1_000_000)
            throw new IllegalArgumentException("Requests must be 0..1000000; drivers must be 1..1000000");
    }

    public double ratio() { return (double) requests / availableDrivers; }
}
