package de.paul2708.cs2stats;

public class ShareCode {

    private final String shareCode;

    private ShareCode(String shareCode) {
        this.shareCode = shareCode;
    }

    public String getMatchId() {
        return null;
    }

    public String getOutcomeId() {
        return null;
    }

    public String getToken() {
        return null;
    }

    public String getShareCode() {
        return shareCode;
    }

    public static ShareCode fromCode(String shareCode) {
        return new ShareCode(shareCode);
    }
}
