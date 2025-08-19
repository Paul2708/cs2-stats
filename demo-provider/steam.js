const SteamUser = require('steam-user');
const GlobalOffensive = require('globaloffensive');
const {decodeShareCode} = require("./sharecode");

const logger = require('./logger.js');

const steamUser = new SteamUser();
const csClient = new GlobalOffensive(steamUser);

let connected = false;

const requests = [];

function connectToSteam(accountName, password) {
    logger.debug("Connecting to Steam...")
    steamUser.logOn({
        accountName: accountName,
        password: password
    })

    setInterval(() => {
        connected = false;
        logger.debug("Relogging to Steam")
        steamUser.relog()
    }, 20 * 60 * 1000);
}

async function requestGame(shareCode, callback) {
    while (!connected) {
        await new Promise(resolve => setTimeout(resolve, 1000));
    }

    const {matchId, outcomeId, token} = decodeShareCode(shareCode)

    const request = {
        shareCode: shareCode,
        matchId: matchId,
        outcomeId: outcomeId,
        callback: callback
    }
    requests.push(request);

    csClient.requestGame(shareCode)

    logger.debug("Waiting to receive match from Steam client")
}


steamUser.on('loggedOn', () => {
    logger.info("Steam user logged in to Steam");

    steamUser.gamesPlayed([730]);

    connected = true;
});

csClient.on('matchList', (matches, data) => {
    logger.debug("Received match list from Steam client")

    if (matches.length !== 1) {
        logger.error(`Match list contains ${matches.length} entries, abort request.`);
        return;
    }

    const match = matches[0];
    const matchId = match.matchid;
    const demoUrl = matches[0]["roundstatsall"][matches[0]["roundstatsall"].length - 1]["map"];

    logger.debug(`Received match with match ID ${matchId} and demo URL ${demoUrl}`)

    const requestItem = requests.find(req => req.matchId === matchId);

    if (requestItem) {
        const matchResponse = {
            matchId: matchId,
            demoUrl: demoUrl,
            matchTime: data["matches"][0]["matchtime"],
        }
        requestItem.callback(matchResponse);

        requests.splice(requests.indexOf(requestItem), 1);
    } else {
        logger.error(`No request found with match id ${matchId}`);
    }
});

module.exports = {connectToSteam, requestGame};
