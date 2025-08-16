const {parseHeader, parseEvent, parseTicks} = require("@laihoe/demoparser2");
const logger = require('./logger.js');

function parseDemo(demoPath) {
    let gameEndTick = Math.max(...parseEvent(demoPath, "round_end").map(x => x.tick))

    let fields = [
        "kills_total", "deaths_total", "assists_total", "headshot_kills_total", "ace_rounds_total", "4k_rounds_total",
        "3k_rounds_total", "damage_total", "utility_damage_total", "enemies_flashed_total", "cash_earned_total",
        "mvps",
        "rank_if_win", "rank_if_loss", "rank_if_tie",
        "team_score_first_half", "team_score_second_half", "total_rounds_played", "team_score_overtime"
    ]
    let scoreboard = parseTicks(demoPath, fields, [gameEndTick])

    logger.debug(`Parser parsed the scoreboard: ${JSON.stringify(scoreboard)}`)

    const playerStats = []

    for (let i = 0; i < scoreboard.length; i++) {
        const player = scoreboard[i];

        let rank;
        const totalRounds = player.total_rounds_played;
        const teamRounds = player.team_score_first_half + player.team_score_second_half + player.team_score_overtime;
        const enemyRounds = totalRounds - teamRounds;

        if (teamRounds > enemyRounds) {
            rank = player.rank_if_win;
        } else if (teamRounds < enemyRounds) {
            rank = player.rank_if_loss;
        } else {
            rank = player.rank_if_tie;
        }

        playerStats.push({
            "name": player.name,
            "steamId": player.steamid,
            "deaths": player.deaths_total,
            "kills": player.kills_total,
            "assists": player.assists_total,
            "headshotKills": player.headshot_kills_total,
            "aces": player.ace_rounds_total,
            "fourKRounds": player["4k_rounds_total"],
            "threeKRounds": player["3k_rounds_total"],
            "damage": player.damage_total,
            "utilityDamage": player.utility_damage_total,
            "enemiesFlashed": player.enemies_flashed_total,
            "cashEarned": player.cash_earned_total,
            "mvps": player.mvps,
            "updatedRank": rank,
            "totalRounds": totalRounds,
            "teamRounds": teamRounds
        })
    }

    return {
        map: parseHeader(demoPath)["map_name"],
        scoreboard: playerStats,
    };
}


module.exports = {parseDemo};
