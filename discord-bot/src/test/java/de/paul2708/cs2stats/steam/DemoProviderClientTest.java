package de.paul2708.cs2stats.steam;

import io.github.cdimascio.dotenv.Dotenv;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class DemoProviderClientTest {

    private static Dotenv dotenv;

    @BeforeAll
    public static void setup() {
        DemoProviderClientTest.dotenv = Dotenv.configure()
                .filename(".env.test")
                .load();
    }

    @Test
    void testRequested() {
        DemoProviderClient demoProviderClient = new DemoProviderClient(dotenv);

        try {
            Optional<Match> match = demoProviderClient.requestMatch(ShareCode.fromCode("CSGO-KDoMb-JO4FR-7R3QX-jRvD9-ApWjM"));

            assertTrue(match.isPresent());

            assertEquals("de_nuke", match.get().matchDetails().map());
        } catch (Exception e) {
            fail(e);
        }
    }
}
