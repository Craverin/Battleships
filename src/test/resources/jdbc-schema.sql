CREATE TABLE comment
(
    player VARCHAR(64) NOT NULL,
    game VARCHAR(64) NOT NULL,
    comment TEXT NOT NULL,
    commentedOn TIMESTAMP NOT NULL
);

CREATE TABLE rating
(
    player VARCHAR(64) NOT NULL,
    game VARCHAR(64) NOT NULL,
    rating INTEGER NOT NULL,
    ratedOn TIMESTAMP NOT NULL,
    PRIMARY KEY (player, game)
);

CREATE TABLE score
(
    player VARCHAR(64) NOT NULL,
    game VARCHAR(64) NOT NULL,
    points INTEGER NOT NULL,
    playedOn TIMESTAMP NOT NULL
);