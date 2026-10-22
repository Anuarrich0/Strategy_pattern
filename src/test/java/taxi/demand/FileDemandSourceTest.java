package taxi.demand;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import taxi.model.Ride;
import taxi.pricing.SurgePricing;
import static org.junit.jupiter.api.Assertions.*;

class FileDemandSourceTest {
    @TempDir Path directory;

    @Test void unchangedStrategyReadsUpdatedFileOnEveryCall() throws Exception {
        Path file = directory.resolve("demand.properties");
        Files.writeString(file, "requests=20\navailableDrivers=10\n");
        SurgePricing strategy = new SurgePricing(new FileDemandSource(file));
        Ride ride = new Ride(10, 15, false, false);
        assertEquals(4000, strategy.price(ride));
        Files.writeString(file, "requests=10\navailableDrivers=10\n");
        assertEquals(2000, strategy.price(ride));
    }

    @Test void missingMalformedAndInvalidFilesFailExplicitly() throws Exception {
        Path file = directory.resolve("demand.properties");
        FileDemandSource source = new FileDemandSource(file);
        assertThrows(IllegalArgumentException.class, source::current);
        for (String contents : new String[]{"requests=20", "requests=x\navailableDrivers=10",
                "requests=20\navailableDrivers=0", "requests=-1\navailableDrivers=10"}) {
            Files.writeString(file, contents);
            assertThrows(IllegalArgumentException.class, source::current);
        }
    }
}
