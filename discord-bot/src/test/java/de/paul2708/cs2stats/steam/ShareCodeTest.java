package de.paul2708.cs2stats.steam;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ShareCodeTest {

    @Test
    void testDecoding() {
        ShareCode shareCode = ShareCode.fromCode("CSGO-GChfW-efWkV-wQFrd-8Mmdp-rOpYF");

        assertEquals("3761093770808918614", shareCode.matchId());
        assertEquals("3761099962004275605", shareCode.outcomeId());
        assertEquals("48107", shareCode.token());
    }
}
