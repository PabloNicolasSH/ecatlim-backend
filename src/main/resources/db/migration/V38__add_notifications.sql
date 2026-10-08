CREATE TABLE notification
(
    id              INT AUTO_INCREMENT NOT NULL,
    user_id         INT                NOT NULL,
    type            VARCHAR(50)        NOT NULL,
    title           VARCHAR(255)       NOT NULL,
    description     VARCHAR(500)       NULL,
    link            VARCHAR(255)       NULL,
    requires_action BIT(1)             NOT NULL,
    reference_id    INT                NULL,
    created_at      datetime           NOT NULL,
    read_at         datetime           NULL,
    resolved_at     datetime           NULL,
    CONSTRAINT pk_notification PRIMARY KEY (id)
);

ALTER TABLE notification
    ADD CONSTRAINT FK_NOTIFICATION_ON_USER FOREIGN KEY (user_id) REFERENCES user (id) ON DELETE CASCADE;

CREATE INDEX idx_notification_user_created ON notification (user_id, created_at);
CREATE INDEX idx_notification_reference ON notification (user_id, type, reference_id);
