package de.paul2708.cs2stats.steam;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.cdimascio.dotenv.Dotenv;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;

public class DemoProviderClient {

    private static final String ENDPOINT = "http://%s:%d/demo/%s";

    private final String baseUrl;
    private final int port;

    private final HttpClient client;

    public DemoProviderClient(Dotenv dotenv) {
        this.baseUrl = dotenv.get("MATCH_SERVICE_BASE_URL");
        this.port = Integer.parseInt(dotenv.get("MATCH_SERVICE_PORT"));

        this.client = HttpClient.newHttpClient();
    }

    public Optional<Match> requestMatch(ShareCode shareCode) throws Exception {
        String url = String.format(
                ENDPOINT, baseUrl, port, shareCode.shareCode()
        );

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 500) {
            throw new Exception("Failed to get match information: " + response.body());
        }
        if (response.statusCode() == 404) {
            return Optional.empty();
        }

        if (response.statusCode() == 200) {
            ObjectMapper mapper = new ObjectMapper();
            return Optional.ofNullable(mapper.readValue(response.body(), Match.class));
        }

        throw new IllegalStateException("Unexpected response code: " + response.statusCode() + " Body: " + response.body());
    }
}
