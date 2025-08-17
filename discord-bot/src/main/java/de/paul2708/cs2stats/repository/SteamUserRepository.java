package de.paul2708.cs2stats.repository;

import de.chojo.sadu.queries.api.call.Call;
import de.paul2708.cs2stats.entity.SteamUser;

import java.util.List;
import java.util.Optional;

import static de.chojo.sadu.queries.api.query.Query.query;

public class SteamUserRepository {

    public void create(SteamUser user) {
        query("INSERT INTO steamusers(steamId, initialShareCode, authenticationCode, lastKnownShareCode, discordUserName) VALUES(:steamId, :initialShareCode, :authenticationCode, :lastKnownShareCode, :discordUserName)")
                .single(Call.of()
                        .bind("steamId", user.steamId())
                        .bind("initialShareCode", user.initialShareCode().shareCode())
                        .bind("authenticationCode", user.authenticationCode())
                        .bind("lastKnownShareCode", user.lastKnownShareCode().shareCode())
                        .bind("discordUserName", user.discordUserName()))
                .insert();
    }

    public void updateLastKnownShareCode(String steamId, String updatedShareCode) {
        query("UPDATE steamusers SET lastKnownShareCode = :lastKnownShareCode WHERE steamId = :steamId")
                .single(Call.of()
                        .bind("lastKnownShareCode", updatedShareCode)
                        .bind("steamId", steamId))
                .update();
    }

    public Optional<SteamUser> findBySteamId(String steamId) {
        return query("SELECT * FROM steamusers where steamId = :steamId")
                .single(Call.of()
                        .bind("steamId", steamId)
                )
                .map(SteamUser.map())
                .first();
    }

    public Optional<SteamUser> findByDiscordName(String discordUserName) {
        return query("SELECT * FROM steamusers where discordUserName = :discordUserName")
                .single(Call.of()
                        .bind("discordUserName", discordUserName)
                )
                .map(SteamUser.map())
                .first();
    }

    public List<SteamUser> findAll() {
        return query("SELECT * FROM steamusers")
                .single()
                .map(SteamUser.map())
                .all();
    }
}
