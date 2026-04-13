CREATE TABLE player_cards
(
    id          UUID PRIMARY KEY                  DEFAULT gen_random_uuid(),
    user_id     UUID                     NOT NULL,
    player_id   INTEGER                  NOT NULL,
    player_name VARCHAR(100)             NOT NULL,
    team_id     INTEGER                  NOT NULL,
    team_name   VARCHAR(100)             NOT NULL,
    tier        VARCHAR(10)              NOT NULL DEFAULT 'BRONZE',
    obtained_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now()
);

CREATE TABLE collection_progress
(
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id        UUID    NOT NULL UNIQUE,
    total_cards    INTEGER NOT NULL DEFAULT 0,
    unique_players INTEGER NOT NULL DEFAULT 0,
    bronze_count   INTEGER NOT NULL DEFAULT 0,
    silver_count   INTEGER NOT NULL DEFAULT 0,
    gold_count     INTEGER NOT NULL DEFAULT 0,
    version        BIGINT  NOT NULL DEFAULT 0
);

CREATE INDEX idx_player_cards_user_id ON player_cards (user_id);
CREATE INDEX idx_player_cards_user_player ON player_cards (user_id, player_id);