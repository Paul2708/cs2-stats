package de.paul2708.cs2stats.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import de.chojo.sadu.queries.api.call.Call;
import de.chojo.sadu.queries.api.results.writing.insertion.InsertionResult;
import de.paul2708.cs2stats.steam.Match;

import java.util.List;
import java.util.Optional;

import static de.chojo.sadu.queries.api.query.Query.query;

public class MatchRepository {

    private final ObjectWriter objectWriter;

    public MatchRepository() {
        this.objectWriter = new ObjectMapper().writer();
    }

    public InsertionResult create(Match match) {
        String statsAsJson;
        try {
            statsAsJson = objectWriter.writeValueAsString(match.matchDetails().scoreboard());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        return query("INSERT INTO matches(matchId, matchTime, map, stats) VALUES(:matchId, :matchTime, :map, :stats::jsonb)")
                .single(Call.of()
                        .bind("matchId", match.matchId())
                        .bind("matchTime", match.matchTime())
                        .bind("map", match.matchDetails().map())
                        .bind("stats", statsAsJson))
                .insert();
    }

    public Optional<Match> findMatchById(String matchId) {
        return query("SELECT * FROM matches where matchId = :matchId")
                .single(Call.of()
                        .bind("matchId", matchId)
                )
                .map(Match.map())
                .first();
    }

    public List<Match> findAll() {
        return query("SELECT * FROM matches ORDER BY matchTime")
                .single(Call.of())
                .map(Match.map())
                .all();
    }
}
