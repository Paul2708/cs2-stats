const SteamUser = require('steam-user');
const GlobalOffensive = require('globaloffensive');
const {decodeShareCode} = require("./sharecode");


const steamUser = new SteamUser();
const csClient = new GlobalOffensive(steamUser);

const requests = [];

function connectToSteam(accountName, password) {
    steamUser.logOn({
        accountName: accountName,
        password: password
    })
}

function requestGame(shareCode, callback) {
    const {matchId, outcomeId, token} = decodeShareCode(shareCode)

    const request = {
        shareCode: shareCode,
        matchId: matchId,
        outcomeId: outcomeId,
        callback: callback
    }
    requests.push(request);

    console.log(request)

    csClient.requestGame(shareCode)
}


steamUser.on('loggedOn', () => {
    console.log('Logged on');
    steamUser.gamesPlayed([730]);
});

csClient.on('matchList', (matches, data) => {
    console.log('Match list', matches);

    if (matches.length !== 1) {
        console.log("Matches contains " + matches.length + " matches.");
        return;
    }

    const match = matches[0];
    const matchId = match.matchid;
    const reservationId = matches[0]["roundstatsall"][matches[0]["roundstatsall"].length - 1]["reservationid"];
    const demoUrl = matches[0]["roundstatsall"][matches[0]["roundstatsall"].length - 1]["map"];

    console.log("Received:")
    console.log(matchId, reservationId, demoUrl)

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
        console.log('No request found with match id ' + matchId);
    }
});

module.exports = {connectToSteam, requestGame};
