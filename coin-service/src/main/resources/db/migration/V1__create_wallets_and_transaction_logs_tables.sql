CREATE TABLE wallets
(
    id      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID    NOT NULL UNIQUE,
    balance INTEGER NOT NULL DEFAULT 0,
    version BIGINT  NOT NULL DEFAULT 0
);

CREATE TABLE transaction_logs
(
    id         UUID PRIMARY KEY                  DEFAULT gen_random_uuid(),
    user_id    UUID                     NOT NULL,
    amount     INTEGER                  NOT NULL,
    type       VARCHAR(10)              NOT NULL,
    reason     VARCHAR(30)              NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now()
);

CREATE INDEX idx_wallet_user_id ON wallets (user_id);
CREATE INDEX idx_transaction_user_id ON transaction_logs (user_id);