CREATE TABLE team (
                      id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                      name VARCHAR(100) NOT NULL,
                      short_name VARCHAR(20),
                      tla VARCHAR(4),
                      external_id INTEGER UNIQUE NOT NULL,
                      team_rarity_weight INTEGER DEFAULT 100
);

CREATE TABLE player (
                        id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                        external_id INTEGER UNIQUE NOT NULL,
                        name VARCHAR(100) NOT NULL,
                        team_id UUID REFERENCES team(id),
                        position VARCHAR(20),
                        is_active BOOLEAN DEFAULT true,
                        player_rarity_weight INTEGER DEFAULT 100
);

CREATE TABLE game (
                       id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                       external_id INTEGER UNIQUE NOT NULL,
                       home_team_id UUID REFERENCES team(id),
                       away_team_id UUID REFERENCES team(id),
                       gameweek INTEGER,
                       home_score INTEGER,
                       away_score INTEGER,
                       status VARCHAR(20) NOT NULL DEFAULT 'SCHEDULED',
                       kickoff_time TIMESTAMP WITH TIME ZONE,
                       completed_at TIMESTAMP WITH TIME ZONE
);