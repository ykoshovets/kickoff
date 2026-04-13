CREATE TABLE trades
(
    id                UUID PRIMARY KEY                  DEFAULT gen_random_uuid(),
    initiator_id      UUID                     NOT NULL,
    receiver_id       UUID                     NOT NULL,
    offered_card_id   UUID                     NOT NULL,
    requested_card_id UUID                     NOT NULL,
    status            VARCHAR(20)              NOT NULL DEFAULT 'PENDING',
    created_at        TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    expires_at        TIMESTAMP WITH TIME ZONE NOT NULL,
    resolved_at       TIMESTAMP WITH TIME ZONE,
    version           BIGINT                   NOT NULL DEFAULT 0
);

CREATE INDEX idx_trades_initiator ON trades (initiator_id);
CREATE INDEX idx_trades_receiver ON trades (receiver_id);
CREATE INDEX idx_trades_status ON trades (status);