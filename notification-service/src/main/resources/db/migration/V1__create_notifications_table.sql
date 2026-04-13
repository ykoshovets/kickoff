CREATE TABLE notifications
(
    id         UUID PRIMARY KEY                  DEFAULT gen_random_uuid(),
    user_id    UUID                     NOT NULL,
    type       VARCHAR(50)              NOT NULL,
    message    TEXT                     NOT NULL,
    is_read    BOOLEAN                  NOT NULL DEFAULT false,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now()
);

CREATE INDEX idx_notifications_user_id ON notifications (user_id);
CREATE INDEX idx_notifications_user_unread ON notifications (user_id, is_read);