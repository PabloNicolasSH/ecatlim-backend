DROP TABLE IF EXISTS event_enrollments;

CREATE TABLE event_enrollment
(
    id              INT AUTO_INCREMENT NOT NULL,
    event_id        INT                NOT NULL,
    user_id         INT                NOT NULL,
    lesson_block_id INT                NOT NULL,
    payment_status  VARCHAR(50)        NOT NULL,
    has_attended    BOOLEAN            NOT NULL DEFAULT FALSE,
    CONSTRAINT pk_eventenrollment PRIMARY KEY (id)
);

ALTER TABLE event_enrollment
    ADD CONSTRAINT FK_EVENTENROLLMENT_ON_EVENT
        FOREIGN KEY (event_id) REFERENCES event (id) ON DELETE CASCADE;

ALTER TABLE event_enrollment
    ADD CONSTRAINT FK_EVENTENROLLMENT_ON_USER
        FOREIGN KEY (user_id) REFERENCES user (id) ON DELETE CASCADE;

ALTER TABLE event_enrollment
    ADD CONSTRAINT FK_EVENTENROLLMENT_ON_LESSON_BLOCK
        FOREIGN KEY (lesson_block_id) REFERENCES lesson_block (id) ON DELETE CASCADE;