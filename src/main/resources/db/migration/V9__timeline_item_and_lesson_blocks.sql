CREATE TABLE event_enrollments
(
    event_id INT NOT NULL,
    user_id  INT NOT NULL,
    CONSTRAINT pk_event_enrollments PRIMARY KEY (event_id, user_id)
);

CREATE TABLE event_lesson_blocks
(
    event_id        INT NOT NULL,
    lesson_block_id INT NOT NULL,
    CONSTRAINT pk_event_lesson_blocks PRIMARY KEY (event_id, lesson_block_id)
);

CREATE TABLE timeline_item
(
    id                   INT AUTO_INCREMENT NOT NULL,
    title                VARCHAR(255)       NOT NULL,
    `description`        VARCHAR(255)       NULL,
    start_time           datetime           NOT NULL,
    end_time             datetime           NOT NULL,
    item_type            VARCHAR(255)       NOT NULL,
    education_session_id INT                NULL,
    event_id             INT                NULL,
    CONSTRAINT pk_timelineitem PRIMARY KEY (id)
);

ALTER TABLE timeline_item
    ADD CONSTRAINT uc_timelineitem_education_session UNIQUE (education_session_id);

ALTER TABLE timeline_item
    ADD CONSTRAINT FK_TIMELINEITEM_ON_EDUCATION_SESSION FOREIGN KEY (education_session_id) REFERENCES education_session (id);

ALTER TABLE timeline_item
    ADD CONSTRAINT FK_TIMELINEITEM_ON_EVENT FOREIGN KEY (event_id) REFERENCES event (id);

ALTER TABLE event_enrollments
    ADD CONSTRAINT fk_eveenr_on_event FOREIGN KEY (event_id) REFERENCES event (id);

ALTER TABLE event_enrollments
    ADD CONSTRAINT fk_eveenr_on_user FOREIGN KEY (user_id) REFERENCES user (id);

ALTER TABLE event_lesson_blocks
    ADD CONSTRAINT fk_evelesblo_on_event FOREIGN KEY (event_id) REFERENCES event (id);

ALTER TABLE event_lesson_blocks
    ADD CONSTRAINT fk_evelesblo_on_lesson_block FOREIGN KEY (lesson_block_id) REFERENCES lesson_block (id);

ALTER TABLE education_session
    DROP COLUMN `description`;

ALTER TABLE education_session
    DROP COLUMN hours;

ALTER TABLE education_session
    DROP COLUMN title;
