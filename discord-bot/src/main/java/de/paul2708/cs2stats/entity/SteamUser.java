package de.paul2708.cs2stats.entity;

import de.chojo.sadu.mapper.annotation.MappingProvider;
import de.chojo.sadu.mapper.rowmapper.RowMapping;
import de.paul2708.cs2stats.ShareCode;

public record SteamUser(String steamId, ShareCode shareCode, String authenticationCode) {

    @MappingProvider({"user"})
    public static RowMapping<SteamUser> map() {
        return row -> new SteamUser(
                row.getString("steamId"),
                ShareCode.fromCode(row.getString("shareCode")),
                row.getString("authenticationCode")
        );
    }
}