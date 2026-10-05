CREATE TABLE recognition_request
(
    id              INT AUTO_INCREMENT NOT NULL,
    user_id         INT                NOT NULL,
    lesson_block_id INT                NOT NULL,
    type            VARCHAR(255)       NOT NULL,
    status          VARCHAR(255)       NOT NULL,
    created_at      datetime(6)        NOT NULL,
    updated_at      datetime(6)        NOT NULL,
    CONSTRAINT pk_recognitionrequest PRIMARY KEY (id),
    CONSTRAINT FK_RECOGNITIONREQUEST_ON_USER FOREIGN KEY (user_id) REFERENCES user (id),
    CONSTRAINT FK_RECOGNITIONREQUEST_ON_LESSON_BLOCK FOREIGN KEY (lesson_block_id) REFERENCES lesson_block (id)
);

CREATE TABLE recognition_message
(
    id         INT AUTO_INCREMENT NOT NULL,
    request_id INT                NOT NULL,
    author_id  INT                NOT NULL,
    kind       VARCHAR(255)       NOT NULL,
    comment    VARCHAR(2000)      NULL,
    created_at datetime(6)        NOT NULL,
    CONSTRAINT pk_recognitionmessage PRIMARY KEY (id),
    CONSTRAINT FK_RECOGNITIONMESSAGE_ON_REQUEST FOREIGN KEY (request_id) REFERENCES recognition_request (id) ON DELETE CASCADE,
    CONSTRAINT FK_RECOGNITIONMESSAGE_ON_AUTHOR FOREIGN KEY (author_id) REFERENCES user (id)
);

CREATE TABLE recognition_message_files
(
    message_id INT NOT NULL,
    file_id    INT NOT NULL,
    CONSTRAINT pk_recognition_message_files PRIMARY KEY (message_id, file_id),
    CONSTRAINT uc_recognition_message_files_file UNIQUE (file_id),
    CONSTRAINT FK_RECOGNITIONMESSAGEFILES_ON_MESSAGE FOREIGN KEY (message_id) REFERENCES recognition_message (id) ON DELETE CASCADE,
    CONSTRAINT FK_RECOGNITIONMESSAGEFILES_ON_FILE FOREIGN KEY (file_id) REFERENCES user_file (id)
);

CREATE TABLE recognition_request_commission
(
    request_id INT NOT NULL,
    user_id    INT NOT NULL,
    CONSTRAINT pk_recognition_request_commission PRIMARY KEY (request_id, user_id),
    CONSTRAINT FK_RECOGNITIONCOMMISSION_ON_REQUEST FOREIGN KEY (request_id) REFERENCES recognition_request (id) ON DELETE CASCADE,
    CONSTRAINT FK_RECOGNITIONCOMMISSION_ON_USER FOREIGN KEY (user_id) REFERENCES user (id)
);

CREATE INDEX idx_recognition_request_user_block ON recognition_request (user_id, lesson_block_id);
