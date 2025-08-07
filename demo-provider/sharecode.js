// Implementation is based on
// https://github.com/ValvePython/csgo/blob/ed81efa8c36122e882ffa5247be1b327dbd20850/csgo/sharecode.py#L16

const dictionary = 'ABCDEFGHJKLMNOPQRSTUVWXYZabcdefhijkmnopqrstuvwxyz23456789';
const _bitmask64 = (1n << 64n) - 1n;

function _swapEndianness(number) {
    let result = 0n;
    for (let n = 0n; n < 144n; n += 8n) {
        result = (result << 8n) + ((number >> n) & 0xFFn);
    }
    return result;
}

function decodeShareCode(code) {
    const regex = new RegExp(`^(CSGO)?(-?[${dictionary}]{5}){5}$`);
    if (!regex.test(code)) {
        throw new Error('Invalid share code');
    }

    code = code.replace(/CSGO-|-/g, '').split('').reverse().join('');

    let a = 0n;
    for (const c of code) {
        a = a * BigInt(dictionary.length) + BigInt(dictionary.indexOf(c));
    }

    a = _swapEndianness(a);

    return {
        matchId: (a & _bitmask64).toString(),
        outcomeId: ((a >> 64n) & _bitmask64).toString(),
        token: ((a >> 128n) & 0xFFFFn).toString()
    };
}

module.exports = {decodeShareCode};
