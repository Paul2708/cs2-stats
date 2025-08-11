require('dotenv').config()

const express = require('express')
const {connectToSteam, requestGame} = require("./steam");
const {downloadDemo} = require("./downloader");
const {parseDemo} = require("./parser");

const logger = require('./logger.js');

const app = express()
const port = 3000

connectToSteam(process.env.STEAM_USERNAME, process.env.STEAM_PASSWORD)

app.get('/demo/:shareCode', (req, res) => {
    logger.info(`Received request for share code ${req.params.shareCode}`)

    requestGame(req.params.shareCode, (matchResponse) => {
        const matchId = matchResponse.matchId;
        const demoUrl = matchResponse.demoUrl;
        const matchTime = matchResponse.matchTime;

        downloadDemo(demoUrl, (path) => {
            if (path.startsWith("error: Google Edge Cache")) {
                logger.error(`Failed to download demo for match ID ${matchId}: ${path}`)
                res.status(404).send(path);
            } else if (path.startsWith("error")) {
                logger.error(`Failed to download demo for match ID ${matchId}: ${path}`)
                res.status(500).send(path);
            } else {
                const response = {
                    matchId: matchId,
                    demoUrl: demoUrl,
                    matchDetails: parseDemo(path),
                    matchTime: matchTime,
                }
                res.status(200).send(response)

                logger.debug(`Sent response for match id ${matchId}: ${JSON.stringify(response)}`)
            }
        })
    })
})

app.listen(port, () => {
    logger.info(`Demo provider is up and running on port ${port}`)
})
