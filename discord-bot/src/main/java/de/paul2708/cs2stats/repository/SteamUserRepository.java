package de.paul2708.cs2stats.repository;

import de.chojo.sadu.queries.api.call.Call;
import de.chojo.sadu.queries.api.results.writing.insertion.InsertionResult;
import de.paul2708.cs2stats.entity.SteamUser;

import java.util.List;

import static de.chojo.sadu.queries.api.query.Query.query;

public class SteamUserRepository {

    public InsertionResult save(SteamUser user) {
        return query("INSERT INTO steamusers(steamId, shareCode, authenticationCode) VALUES(:steamId, :shareCode, :authenticationCode)")
                .single(Call.of()
                        .bind("steamId", user.steamId())
                        .bind("shareCode", user.shareCode().shareCode())
                        .bind("authenticationCode", user.authenticationCode()))
                .insert();
    }

    public List<SteamUser> findAll() {
        return query("SELECT * FROM steamusers")
                .single()
                .map(SteamUser.map())
                .all();
    }
}
