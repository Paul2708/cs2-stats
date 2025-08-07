package de.paul2708.cs2stats.steam;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.chojo.sadu.mapper.annotation.MappingProvider;
import de.chojo.sadu.mapper.rowmapper.RowMapping;

import java.util.List;

public record Match(
        String matchId,
        String demoUrl,
        MatchDetails matchDetails,
        long matchTime
) {

    @MappingProvider({"match"})
    public static RowMapping<Match> map() {
        return row -> new Match(
                row.getString("matchId"),
                "ignored",
                new MatchDetails(row.getString("map"), parsePlayerStats(row.getString("stats"))),
                row.getLong("matchTime")
        );
    }

    private static List<PlayerStats> parsePlayerStats(String json) {
        ObjectMapper mapper = new ObjectMapper();

        try {
            return mapper.readValue(json, new TypeReference<List<PlayerStats>>() {
            });
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
