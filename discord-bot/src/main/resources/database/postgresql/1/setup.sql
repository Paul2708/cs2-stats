CREATE TABLE steamusers
(
    steamId            VARCHAR(255) PRIMARY KEY,
    initialShareCode   VARCHAR(255) NOT NULL,
    authenticationCode VARCHAR(255) NOT NULL,
    lastKnownShareCode VARCHAR(255) NOT NULL
);

CREATE TABLE matches
(
    matchId   VARCHAR(255) PRIMARY KEY,
    matchTime BIGINT       NOT NULL,
    map       VARCHAR(255) NOT NULL,
    stats     JSON         NOT NULL
);