package taxi.model;

public record Ride(double distanceKm, int durationMinutes, boolean airport, boolean premium) {
    public Ride {
        if (!Double.isFinite(distanceKm) || distanceKm < 0 || distanceKm > 10_000)
            throw new IllegalArgumentException("Distance must be 0..10000 km");
        if (durationMinutes < 0 || durationMinutes > 10_000)
            throw new IllegalArgumentException("Duration must be 0..10000 minutes");
    }
}
