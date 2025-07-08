package de.paul2708.cs2stats.repository;

import de.chojo.sadu.queries.api.call.Call;
import de.chojo.sadu.queries.api.results.writing.insertion.InsertionResult;
import de.chojo.sadu.queries.call.adapter.UUIDAdapter;
import de.paul2708.cs2stats.entity.SteamUser;

import javax.sql.DataSource;
import java.util.UUID;

import static de.chojo.sadu.queries.api.query.Query.query;

public class SteamUserRepository {

    private final DataSource dataSource;

    public SteamUserRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void save(SteamUser user) {
        // TODO: Implement me
        InsertionResult change =
                query("INSERT INTO users(uuid, name) VALUES(:uuid::uuid,?)")
                // Create a new call
                // First parameter is named and second indexed
                .single(Call.of().bind("uuid", UUID.randomUUID(), UUIDAdapter.AS_STRING).bind("someone"))
                // Insert the data
                .insert();
    }
}
