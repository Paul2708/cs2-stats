package de.paul2708.cs2stats.steam;

import io.github.cdimascio.dotenv.Dotenv;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class SteamClientTest {

    private static Dotenv dotenv;

    @BeforeAll
    public static void setup() {
        SteamClientTest.dotenv = Dotenv.configure()
                .filename(".env.test")
                .load();
    }

    @Test
    void printRequestedShareCodes() {
        SteamClient steamClient = new SteamClient(dotenv);

        try {
            List<ShareCode> shareCodes = steamClient.requestConsecutiveCodes(
                    dotenv.get("STEAM_TEST_USER_ID"),
                    dotenv.get("STEAM_TEST_USER_AUTHENTICATION_CODE"),
                    dotenv.get("STEAM_TEST_USER_KNOWN_SHARE_CODE")
            );

            assertFalse(shareCodes.isEmpty());

            for (ShareCode shareCode : shareCodes) {
                System.out.println(shareCode);
            }
        } catch (IOException | InterruptedException e) {
            fail(e);
        }
    }

    @Test
    void requestSteamIdFromExistingUser() {
        SteamClient steamClient = new SteamClient(dotenv);

        try {
            Optional<String> steamIdOpt = steamClient.requestSteamId("sharebin");

            assertTrue(steamIdOpt.isPresent());
            assertEquals("76561198155949048", steamIdOpt.get());
        } catch (IOException | InterruptedException e) {
            fail(e);
        }
    }

    @Test
    void requestSteamIdFromNonExistingUser() {
        SteamClient steamClient = new SteamClient(dotenv);

        try {
            Optional<String> steamIdOpt = steamClient.requestSteamId("regeurgeigwgerigerg");

            assertFalse(steamIdOpt.isPresent());
        } catch (IOException | InterruptedException e) {
            fail(e);
        }
    }

    @Test
    void checkIfPlayerExists() {
        SteamClient steamClient = new SteamClient(dotenv);

        try {
            boolean exists = steamClient.requestExistence("76561198155949048");

            assertTrue(exists);
        } catch (IOException | InterruptedException e) {
            fail(e);
        }
    }

    @Test
    void checkIfPlayerDoesNotExist() {
        SteamClient steamClient = new SteamClient(dotenv);

        try {
            boolean exists = steamClient.requestExistence("sharebin");

            assertFalse(exists);
        } catch (IOException | InterruptedException e) {
            fail(e);
        }
    }
}
