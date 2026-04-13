CREATE TABLE fantasy_teams
(
    id           UUID PRIMARY KEY                  DEFAULT gen_random_uuid(),
    user_id      UUID                     NOT NULL,
    gameweek     INTEGER                  NOT NULL,
    submitted_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    CONSTRAINT uq_fantasy_team_user_gameweek UNIQUE (user_id, gameweek)
);

CREATE TABLE fantasy_team_players
(
    fantasy_team_id UUID NOT NULL REFERENCES fantasy_teams (id),
    player_card_id  UUID NOT NULL,
    PRIMARY KEY (fantasy_team_id, player_card_id)
);

CREATE TABLE fantasy_scores
(
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id      UUID    NOT NULL,
    gameweek     INTEGER NOT NULL,
    total_points INTEGER NOT NULL DEFAULT 0,
    breakdown    JSONB,
    CONSTRAINT uq_fantasy_score_user_gameweek UNIQUE (user_id, gameweek)
);

CREATE INDEX idx_fantasy_teams_user_id ON fantasy_teams (user_id);
CREATE INDEX idx_fantasy_scores_user_id ON fantasy_scores (user_id);
CREATE INDEX idx_fantasy_scores_gameweek ON fantasy_scores (gameweek);