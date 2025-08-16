package de.paul2708.cs2stats.steam;

import java.math.BigInteger;
import java.util.regex.Pattern;

// Implementation is based on
// https://github.com/ValvePython/csgo/blob/ed81efa8c36122e882ffa5247be1b327dbd20850/csgo/sharecode.py#L16
public record ShareCode(String shareCode, String matchId, String outcomeId, String token) {

    private static final String DICTIONARY = "ABCDEFGHJKLMNOPQRSTUVWXYZabcdefhijkmnopqrstuvwxyz23456789";
    private static final Pattern SHARE_CODE_PATTERN = Pattern.compile("^(CSGO)?(-?[" + DICTIONARY + "]{5}){5}$");
    private static final BigInteger BITMASK_64 = BigInteger.ONE.shiftLeft(64).subtract(BigInteger.ONE);

    public static final ShareCode NONE = ShareCode.fromCode("CSGO-AAAAA-AAAAA-AAAAA-AAAAA-AAAAA");

    private static BigInteger swapEndianness(BigInteger number) {
        BigInteger result = BigInteger.ZERO;
        for (int n = 0; n < 144; n += 8) {
            BigInteger byteVal = number.shiftRight(n).and(BigInteger.valueOf(0xFF));
            result = result.shiftLeft(8).or(byteVal);
        }
        return result;
    }

    public static ShareCode fromCode(String shareCode) {
        if (!SHARE_CODE_PATTERN.matcher(shareCode).matches()) {
            throw new IllegalArgumentException("Invalid share code: %s".formatted(shareCode));
        }

        String code = shareCode.replace("CSGO-", "").replace("-", "");
        code = new StringBuilder(code).reverse().toString();

        BigInteger a = BigInteger.ZERO;
        for (char c : code.toCharArray()) {
            int index = DICTIONARY.indexOf(c);
            if (index == -1) {
                throw new IllegalArgumentException("Invalid character in share code: %s".formatted(shareCode));
            }
            a = a.multiply(BigInteger.valueOf(DICTIONARY.length())).add(BigInteger.valueOf(index));
        }

        a = swapEndianness(a);

        String matchId = a.and(BITMASK_64).toString();
        String outcomeId = a.shiftRight(64).and(BITMASK_64).toString();
        String token = a.shiftRight(128).and(BigInteger.valueOf(0xFFFF)).toString();

        return new ShareCode(shareCode, matchId, outcomeId, token);
    }
}
