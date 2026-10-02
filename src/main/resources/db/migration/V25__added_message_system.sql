CREATE TABLE chat_message
(
    id        INT AUTO_INCREMENT NOT NULL,
    from_id   INT                NULL,
    to_id     INT                NULL,
    message   VARCHAR(255)       NULL,
    timestamp datetime           NULL,
    CONSTRAINT pk_chatmessage PRIMARY KEY (id)
);

ALTER TABLE chat_message
    ADD CONSTRAINT FK_CHATMESSAGE_ON_FROM FOREIGN KEY (from_id) REFERENCES user (id);

ALTER TABLE chat_message
    ADD CONSTRAINT FK_CHATMESSAGE_ON_TO FOREIGN KEY (to_id) REFERENCES user (id);