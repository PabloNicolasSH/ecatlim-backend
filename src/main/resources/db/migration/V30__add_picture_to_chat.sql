ALTER TABLE chat
    ADD chat_picture_id INT NULL;

ALTER TABLE chat
    ADD CONSTRAINT uc_chat_chat_picture UNIQUE (chat_picture_id);

ALTER TABLE chat
    ADD CONSTRAINT FK_CHAT_ON_CHAT_PICTURE FOREIGN KEY (chat_picture_id) REFERENCES user_file (id);
