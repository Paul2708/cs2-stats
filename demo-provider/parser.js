const {parseHeader, parseEvent, parseTicks} = require("@laihoe/demoparser2");
const logger = require('./logger.js');

function parseDemo(demoPath) {
    let gameEndTick = Math.max(...parseEvent(demoPath, "round_end").map(x => x.tick))

    let fields = [
        "kills_total", "deaths_total",
        "mvps",
        "rank_if_win", "rank_if_loss", "rank_if_tie",
        "team_score_first_half", "team_score_second_half", "total_rounds_played"
    ]
    let scoreboard = parseTicks(demoPath, fields, [gameEndTick])

    logger.debug(`Parser parsed the scoreboard: ${JSON.stringify(scoreboard)}`)

    const playerStats = []

    for (let i = 0; i < scoreboard.length; i++) {
        const player = scoreboard[i];

        let rank;
        const totalRounds = player.total_rounds_played;
        const teamRounds = player.team_score_first_half + player.team_score_second_half;
        const enemyRounds = totalRounds - teamRounds;

        if (totalRounds > enemyRounds) {
            rank = player.rank_if_win;
        } else if (totalRounds < enemyRounds) {
            rank = player.rank_if_loss;
        } else {
            rank = player.rank_if_tie;
        }

        playerStats.push({
            "name": player.name,
            "steamId": player.steamid,
            "deaths": player.deaths_total,
            "kills": player.kills_total,
            "mvps": player.mvps,
            "updatedRank": rank,
        })
    }

    return {
        map: parseHeader(demoPath)["map_name"],
        scoreboard: playerStats,
    };
}


module.exports = {parseDemo};
