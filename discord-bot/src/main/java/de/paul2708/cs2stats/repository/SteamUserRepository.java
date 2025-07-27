package de.paul2708.cs2stats.repository;

import de.chojo.sadu.queries.api.call.Call;
import de.chojo.sadu.queries.api.results.writing.insertion.InsertionResult;
import de.chojo.sadu.queries.api.results.writing.manipulation.ManipulationResult;
import de.paul2708.cs2stats.entity.SteamUser;

import java.util.List;

import static de.chojo.sadu.queries.api.query.Query.query;

public class SteamUserRepository {

    public InsertionResult create(SteamUser user) {
        return query("INSERT INTO steamusers(steamId, initialShareCode, authenticationCode, lastKnownShareCode) VALUES(:steamId, :initialShareCode, :authenticationCode, :lastKnownShareCode)")
                .single(Call.of()
                        .bind("steamId", user.steamId())
                        .bind("initialShareCode", user.initialShareCode().shareCode())
                        .bind("authenticationCode", user.authenticationCode())
                        .bind("lastKnownShareCode", user.lastKnownShareCode().shareCode()))
                .insert();
    }

    public ManipulationResult updateLastKnownShareCode(SteamUser steamUser) {
        return query("UPDATE steamusers SET lastKnownShareCode = 'NEW_SHARE_CODE' WHERE steamId = :steamId")
                .single(Call.of()
                        .bind("lastKnownShareCode", steamUser.lastKnownShareCode().shareCode())
                        .bind("steamId", steamUser.steamId()))
                .update();
    }

    public List<SteamUser> findAll() {
        return query("SELECT * FROM steamusers")
                .single()
                .map(SteamUser.map())
                .all();
    }
}
