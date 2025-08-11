const { createLogger, format, transports } = require('winston');

const { combine, timestamp, printf, colorize } = format;

const logFormat = printf(({ level, message, timestamp }) => {
    return `[${timestamp}] [${level}] ${message}`;
});

const logger = createLogger({
    level: 'debug',
    format: combine(
        colorize({ all: false }), // Only colorizes the level, not the whole line
        timestamp({ format: 'YYYY-MM-DD HH:mm:ss' }),
        logFormat
    ),
    transports: [
        new transports.Console()
    ]
});

module.exports = logger;
