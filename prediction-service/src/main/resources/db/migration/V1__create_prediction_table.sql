CREATE TABLE prediction
(
    id                   UUID PRIMARY KEY                  DEFAULT gen_random_uuid(),
    user_id              UUID                     NOT NULL,
    game_external_id     INTEGER                  NOT NULL,
    gameweek             INTEGER                  NOT NULL,
    predicted_home_score INTEGER                  NOT NULL,
    predicted_away_score INTEGER                  NOT NULL,
    result               VARCHAR(20)                       DEFAULT 'PENDING',
    coins_awarded        INTEGER                           DEFAULT 0,
    evaluated_at         TIMESTAMP WITH TIME ZONE,
    created_at           TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now()
);

CREATE INDEX idx_prediction_user_id ON prediction (user_id);
CREATE INDEX idx_prediction_game_external_id ON prediction (game_external_id);
CREATE INDEX idx_prediction_gameweek ON prediction (gameweek);