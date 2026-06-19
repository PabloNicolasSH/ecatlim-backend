CREATE TABLE event_enrollment
(
    id              INT AUTO_INCREMENT NOT NULL,
    event_id        INT                NOT NULL,
    user_id         INT                NOT NULL,
    lesson_block_id INT                NOT NULL,
    payment_state   VARCHAR(255),
    has_attended    BIT(1)             NOT NULL,
    CONSTRAINT pk_eventenrollment PRIMARY KEY (id)
);

ALTER TABLE event_enrollment
    ADD CONSTRAINT FK_EVENTENROLLMENT_ON_EVENT FOREIGN KEY (event_id) REFERENCES event (id);

ALTER TABLE event_enrollment
    ADD CONSTRAINT FK_EVENTENROLLMENT_ON_LESSON_BLOCK FOREIGN KEY (lesson_block_id) REFERENCES lesson_block (id);

ALTER TABLE event_enrollment
    ADD CONSTRAINT FK_EVENTENROLLMENT_ON_USER FOREIGN KEY (user_id) REFERENCES user (id);

DROP TABLE event_enrollments;