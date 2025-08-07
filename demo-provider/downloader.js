const fs = require('fs');
const http = require('http');

const bz2 = require('unbzip2-stream');
const crypto = require("crypto");

// TODO: Add proper logging
function downloadDemo(url, callback) {
    const uuid = crypto.randomUUID();

    const file = fs.createWriteStream(uuid + '.dem.bz2');
    const pathWithoutExtension = uuid + '.dem';

    http.get(url, (response) => {
        response.pipe(file);
        file.on('finish', () => {
            file.close();
            console.log('Download completed');

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

            fs.createReadStream(file.path)
                .pipe(bz2())
                .pipe(fs.createWriteStream(pathWithoutExtension))
                .on('finish', () => {
                    console.log('Decompression complete');

                    callback(pathWithoutExtension);

                    fs.unlinkSync(file.path);
                    fs.unlinkSync(pathWithoutExtension);
                })
                .on('error', (err) => {
                    console.error('Decompression failed:', err);
                    fs.unlinkSync(pathWithoutExtension);
                    callback("error: " + err.message);
                });
        });
    }).on('error', (err) => {
        fs.unlinkSync(file.path); // delete the file if an error occurs
        console.error('Download failed:', err.message);
        callback("error: " + err.message);
    });
}


module.exports = {downloadDemo};
