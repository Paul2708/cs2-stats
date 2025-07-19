require('dotenv').config()

const express = require('express')
const {connectToSteam, requestGame} = require("./steam");
const {downloadDemo} = require("./downloader");
const {parseDemo} = require("./parser");
const app = express()
const port = 3000

connectToSteam(process.env.STEAM_USERNAME, process.env.STEAM_PASSWORD)

app.get('/demo/:shareCode', (req, res) => {
    requestGame(req.params.shareCode, (matchResponse) => {
        const matchId = matchResponse.matchId;
        const demoUrl = matchResponse.demoUrl;
        const matchTime = matchResponse.matchTime;

        console.log("Fetched: " + matchId, demoUrl)

        downloadDemo(demoUrl, (path) => {
            if (path.startsWith("error")) {
                res.status(500).send(path);
            } else {

                const response = {
                    matchId: matchId,
                    demoUrl: demoUrl,
                    match: parseDemo(path),
                    matchTime: matchTime,
                }
                res.status(200).send(response)
            }
        })
    })
})

app.listen(port, () => {
    console.log(`Example app listening on port ${port}`)
})
