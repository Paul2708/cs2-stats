const fs = require('fs');
const http = require('http');

const bz2 = require('unbzip2-stream');
const crypto = require("crypto");

const logger = require('./logger.js');

function downloadDemo(url, callback) {
    logger.info(`Start to download demo from URL ${url}`)

    const uuid = crypto.randomUUID();

    const file = fs.createWriteStream(uuid + '.dem.bz2');
    const pathWithoutExtension = uuid + '.dem';

    http.get(url, (response) => {
        response.pipe(file);

        file.on('finish', () => {
            file.close();
            logger.info("Download completed.");

            try {
                const data = fs.readFileSync(file.path, 'utf8');

                if (data.includes("Google-Edge-Cache: origin retries exhausted")) {
                    fs.unlinkSync(file.path);
                    callback("error: Google Edge Cache");
                    return;
                }
            } catch (err) {
                fs.unlinkSync(file.path);
                callback("error: Failed to read file");
                return;
            }

            logger.info("Start decompression...");

            fs.createReadStream(file.path)
                .pipe(bz2())
                .pipe(fs.createWriteStream(pathWithoutExtension))
                .on('finish', () => {
                    logger.info('Decompression completed.');

                    callback(pathWithoutExtension);

                    fs.unlinkSync(file.path);
                    fs.unlinkSync(pathWithoutExtension);
                })
                .on('error', (err) => {
                    logger.error('Failed to decompress demo', err);

                    fs.unlinkSync(pathWithoutExtension);
                    callback("error: " + err.message);
                });
        });
    }).on('error', (err) => {
        logger.error('Download failed', err);

        fs.unlinkSync(file.path);
        callback("error: " + err.message);
    }).setTimeout(5 * 60 * 1000, function () {
        logger.error('Download timed out after 2 minutes');
        this.abort();
        fs.unlinkSync(file.path);
        callback("error: Download timeout");
    });
}


module.exports = {downloadDemo};
