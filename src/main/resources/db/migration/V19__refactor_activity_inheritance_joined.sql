CREATE TABLE file_upload_activity
(
    activity_id INT NOT NULL,
    CONSTRAINT pk_fileuploadactivity PRIMARY KEY (activity_id)
);

CREATE TABLE forum_activity
(
    activity_id INT NOT NULL,
    CONSTRAINT pk_forumactivity PRIMARY KEY (activity_id)
);

CREATE TABLE survey_activity
(
    activity_id   INT    NOT NULL,
    is_gradable   BIT(1) NULL,
    max_attempts  INT    NULL,
    passing_score DOUBLE NULL,
    CONSTRAINT pk_surveyactivity PRIMARY KEY (activity_id)
);

ALTER TABLE file_upload_activity
    ADD CONSTRAINT FK_FILEUPLOADACTIVITY_ON_ACTIVITY FOREIGN KEY (activity_id) REFERENCES activity (id);

ALTER TABLE forum_activity
    ADD CONSTRAINT FK_FORUMACTIVITY_ON_ACTIVITY FOREIGN KEY (activity_id) REFERENCES activity (id);

ALTER TABLE survey_activity
    ADD CONSTRAINT FK_SURVEYACTIVITY_ON_ACTIVITY FOREIGN KEY (activity_id) REFERENCES activity (id);

ALTER TABLE activity
    DROP COLUMN is_gradable;

ALTER TABLE activity
    DROP COLUMN max_attempts;

ALTER TABLE activity
    DROP COLUMN passing_score;

ALTER TABLE activity
    MODIFY event_id INT NULL;