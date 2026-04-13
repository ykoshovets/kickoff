CREATE TABLE pack_definitions
(
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    pack_type    VARCHAR(30) NOT NULL UNIQUE,
    card_count   INTEGER     NOT NULL,
    coin_cost    INTEGER     NOT NULL,
    weekly_limit INTEGER
);

CREATE TABLE pack_purchases
(
    id           UUID PRIMARY KEY                  DEFAULT gen_random_uuid(),
    user_id      UUID                     NOT NULL,
    pack_type    VARCHAR(30)              NOT NULL,
    purchased_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now()
);

CREATE INDEX idx_pack_purchases_user_id ON pack_purchases (user_id);
CREATE INDEX idx_pack_purchases_user_type ON pack_purchases (user_id, pack_type);