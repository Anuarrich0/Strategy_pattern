package taxi.model;

public record Driver(String name, double distanceKm, double rating, int idleMinutes) {
    public Driver {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Driver name is required");
        if (!Double.isFinite(distanceKm) || distanceKm < 0 || distanceKm > 10_000)
            throw new IllegalArgumentException("Driver distance must be 0..10000 km");
        if (!Double.isFinite(rating) || rating < 0 || rating > 5)
            throw new IllegalArgumentException("Driver rating must be 0..5");
        if (idleMinutes < 0 || idleMinutes > 1_000_000)
            throw new IllegalArgumentException("Idle time must be 0..1000000 minutes");
    }
}
