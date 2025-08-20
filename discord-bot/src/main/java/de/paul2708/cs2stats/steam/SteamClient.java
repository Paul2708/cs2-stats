package de.paul2708.cs2stats.steam;

import io.github.cdimascio.dotenv.Dotenv;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class SteamClient {

    private static final String SHARE_CODE_ENDPOINT = "https://api.steampowered.com/ICSGOPlayers_730/GetNextMatchSharingCode/v1" +
            "?key=%s&steamid=%s&steamidkey=%s&knowncode=%s";
    private static final String RESOLVE_ENDPOINT = "https://api.steampowered.com/ISteamUser/ResolveVanityURL/v0001/?key=%s&vanityurl=%s";
    private static final String PLAYER_SUMMARY_ENDPOINT = "https://api.steampowered.com/ISteamUser/GetPlayerSummaries/v2/?key=%s&format=json&steamids=%s";

    private final String apiKey;

    private final HttpClient client;

    public SteamClient(Dotenv dotenv) {
        this.apiKey = dotenv.get("STEAM_API_KEY");

        this.client = HttpClient.newHttpClient();
    }

    public List<ShareCode> requestConsecutiveCodes(String steamUserId, String authenticationCode, String initialShareCode)
            throws IOException, InterruptedException {
        List<ShareCode> shareCodes = new ArrayList<>();

        Optional<String> shareCodeIter = Optional.of(initialShareCode);
        while (shareCodeIter.isPresent()) {
            shareCodes.add(ShareCode.fromCode(shareCodeIter.get()));

            shareCodeIter = requestShareCode(steamUserId, authenticationCode, shareCodeIter.get());

            TimeUnit.MILLISECONDS.sleep(500);
        }

        return shareCodes;
    }

    private Optional<String> requestShareCode(String steamUserId, String authenticationCode, String previousShareCode)
            throws IOException, InterruptedException {
        String url = String.format(
                SHARE_CODE_ENDPOINT, apiKey, steamUserId, authenticationCode, previousShareCode
        );

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        /*
        Possible responses:
        - Newer share code exists: 200 {'result': {'nextcode': 'CSGO-fAtwf-Q7XyG-JMTiU-x4Wzp-HDpcG'}}
        - No newer share code exists: 202 {'result': {'nextcode': 'n/a'}}
         */

        if (response.statusCode() == 202) {
            return Optional.empty();
        }

        if (response.statusCode() == 200) {
            JSONObject json = new JSONObject(response.body());
            return Optional.of(json.getJSONObject("result").getString("nextcode"));
        }

        throw new IllegalStateException("Unexpected response code: " + response.statusCode() + " Body: " + response.body());
    }

    public Optional<String> requestSteamId(String steamUserName) throws IOException, InterruptedException {
        String url = String.format(RESOLVE_ENDPOINT, apiKey, steamUserName);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            JSONObject json = new JSONObject(response.body());

            if (json.has("response") && json.getJSONObject("response").has("steamid")) {
                return Optional.ofNullable(json.getJSONObject("response").getString("steamid"));
            }
            if (json.has("response") && json.getJSONObject("response").has("success") && json.getJSONObject("response").getInt("success") == 42) {
                return Optional.empty();
            }
        }

        throw new IllegalStateException("Unexpected response code: " + response.statusCode() + " Body: " + response.body());
    }

    public boolean requestExistence(String steamId) throws IOException, InterruptedException {
        String url = String.format(PLAYER_SUMMARY_ENDPOINT, apiKey, steamId);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            JSONObject json = new JSONObject(response.body());

            if (json.has("response") && json.getJSONObject("response").has("players")) {
                JSONArray players = json.getJSONObject("response").getJSONArray("players");

                return !players.isEmpty();
            }
        }

        throw new IllegalStateException("Unexpected response code: " + response.statusCode() + " Body: " + response.body());
    }
}