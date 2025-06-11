ALTER TABLE chat_message
    DROP FOREIGN KEY FK_CHATMESSAGE_ON_TO;

CREATE TABLE chat
(
    id               INT AUTO_INCREMENT NOT NULL,
    creation_date    datetime,
    chat_name        VARCHAR(255),
    chat_description VARCHAR(255),
    CONSTRAINT pk_chat PRIMARY KEY (id)
);

CREATE TABLE chat_members
(
    chat_id         INT NOT NULL,
    chat_members_id INT NOT NULL
);

ALTER TABLE chat_message
    ADD as_read_at datetime;

ALTER TABLE chat_message
    ADD is_deleted BIT(1) NOT NULL;

ALTER TABLE chat_message
    ADD is_edited BIT(1) NOT NULL;

ALTER TABLE chat_message
    ADD is_read BIT(1) NOT NULL;

ALTER TABLE chat_message
    ADD CONSTRAINT FK_CHATMESSAGE_ON_TO FOREIGN KEY (to_id) REFERENCES chat (id);

ALTER TABLE chat_members
    ADD CONSTRAINT fk_chamem_on_chat FOREIGN KEY (chat_id) REFERENCES chat (id);

ALTER TABLE chat_members
    ADD CONSTRAINT fk_chamem_on_user FOREIGN KEY (chat_members_id) REFERENCES user (id);

ALTER TABLE chat_message
    DROP FOREIGN KEY FK_CHATMESSAGE_ON_TO;

ALTER TABLE chat_message
    ADD chat_id INT NOT NULL;

ALTER TABLE chat_message
    ADD CONSTRAINT FK_CHATMESSAGE_ON_CHAT FOREIGN KEY (chat_id) REFERENCES chat (id);

ALTER TABLE chat_message
    DROP COLUMN to_id;