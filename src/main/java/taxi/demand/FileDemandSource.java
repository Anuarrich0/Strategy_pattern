package taxi.demand;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Properties;

public record FileDemandSource(Path path) implements DemandSource {
    public FileDemandSource { Objects.requireNonNull(path); }

    @Override public Demand current() {
        Properties values = new Properties();
        try (Reader reader = Files.newBufferedReader(path)) {
            values.load(reader);
            return new Demand(Integer.parseInt(values.getProperty("requests", "").strip()),
                    Integer.parseInt(values.getProperty("availableDrivers", "").strip()));
        } catch (IOException | IllegalArgumentException error) {
            throw new IllegalArgumentException("Cannot read demand from " + path + ": " + error.getMessage(), error);
        }
    }
}
