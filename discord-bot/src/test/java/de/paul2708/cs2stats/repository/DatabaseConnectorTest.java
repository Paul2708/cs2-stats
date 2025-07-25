package de.paul2708.cs2stats.repository;

import io.github.cdimascio.dotenv.Dotenv;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class DatabaseConnectorTest {

    private static Dotenv dotenv;

    @BeforeAll
    public static void setup() {
        DatabaseConnectorTest.dotenv = Dotenv.configure()
                .filename(".env.test")
                .load();
    }

    @Test
    void testConnection() {
        DatabaseConnector connector = new DatabaseConnector();
        connector.createDatasource(dotenv);
    }
}
