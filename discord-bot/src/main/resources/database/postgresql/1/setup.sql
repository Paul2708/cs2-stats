CREATE TABLE steamusers
(
    steamId            VARCHAR(255) PRIMARY KEY,
    shareCode          VARCHAR(255) NOT NULL,
    authenticationCode VARCHAR(255) NOT NULL
);