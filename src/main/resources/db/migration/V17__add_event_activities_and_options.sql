CREATE TABLE activity_progress
(
    id             INT AUTO_INCREMENT NOT NULL,
    activity_id    INT                NULL,
    student_id     INT                NOT NULL,
    status         VARCHAR(255)       NULL,
    updated_at     datetime           NULL,
    score          DOUBLE             NULL,
    attempts_count INT                NULL,
    CONSTRAINT pk_activityprogress PRIMARY KEY (id)
);

CREATE TABLE file_submission
(
    id           INT AUTO_INCREMENT NOT NULL,
    activity_id  INT                NOT NULL,
    student_id   INT                NOT NULL,
    file_url     VARCHAR(255)       NOT NULL,
    submitted_at datetime           NULL,
    comment      TEXT               NULL,
    is_approved  BIT(1)             NULL,
    CONSTRAINT pk_filesubmission PRIMARY KEY (id)
);

CREATE TABLE forum_publication
(
    id           INT AUTO_INCREMENT NOT NULL,
    activity_id  INT                NOT NULL,
    author_id    INT                NOT NULL,
    title        VARCHAR(255)       NOT NULL,
    body         TEXT               NOT NULL,
    published_at datetime           NULL,
    CONSTRAINT pk_forumpublication PRIMARY KEY (id)
);

CREATE TABLE survey_option
(
    id          INT AUTO_INCREMENT NOT NULL,
    question_id INT                NOT NULL,
    option_text VARCHAR(255)       NOT NULL,
    is_correct  BIT(1)             NOT NULL,
    CONSTRAINT pk_surveyoption PRIMARY KEY (id)
);

CREATE TABLE survey_question
(
    id            INT AUTO_INCREMENT NOT NULL,
    activity_id   INT                NOT NULL,
    question_text VARCHAR(255)       NOT NULL,
    response_type VARCHAR(255)       NOT NULL,
    CONSTRAINT pk_surveyquestion PRIMARY KEY (id)
);

CREATE TABLE survey_response
(
    id           INT AUTO_INCREMENT NOT NULL,
    question_id  INT                NOT NULL,
    student_id   INT                NOT NULL,
    text_value   VARCHAR(255)       NULL,
    num_value    INT                NULL,
    submitted_at datetime           NULL,
    CONSTRAINT pk_surveyresponse PRIMARY KEY (id)
);

ALTER TABLE activity
    ADD available_at datetime NULL;

ALTER TABLE activity
    ADD created_at datetime NULL;

ALTER TABLE activity
    ADD due_date datetime NULL;

ALTER TABLE activity
    ADD evaluation_method VARCHAR(255) NULL;

ALTER TABLE activity
    ADD is_gradable BIT(1) NULL;

ALTER TABLE activity
    ADD max_attempts INT NULL;

ALTER TABLE activity
    ADD passing_score DOUBLE NULL;

ALTER TABLE activity
    MODIFY available_at datetime NOT NULL;

ALTER TABLE activity
    MODIFY due_date datetime NOT NULL;

ALTER TABLE activity
    MODIFY evaluation_method VARCHAR(255) NOT NULL;

ALTER TABLE activity_progress
    ADD CONSTRAINT FK_ACTIVITYPROGRESS_ON_ACTIVITY FOREIGN KEY (activity_id) REFERENCES activity (id);

ALTER TABLE file_submission
    ADD CONSTRAINT FK_FILESUBMISSION_ON_ACTIVITY FOREIGN KEY (activity_id) REFERENCES activity (id);

ALTER TABLE file_submission
    ADD CONSTRAINT FK_FILESUBMISSION_ON_STUDENT FOREIGN KEY (student_id) REFERENCES user (id);

ALTER TABLE forum_publication
    ADD CONSTRAINT FK_FORUMPUBLICATION_ON_ACTIVITY FOREIGN KEY (activity_id) REFERENCES activity (id);

ALTER TABLE forum_publication
    ADD CONSTRAINT FK_FORUMPUBLICATION_ON_AUTHOR FOREIGN KEY (author_id) REFERENCES user (id);

ALTER TABLE survey_option
    ADD CONSTRAINT FK_SURVEYOPTION_ON_QUESTION FOREIGN KEY (question_id) REFERENCES survey_question (id);

ALTER TABLE survey_question
    ADD CONSTRAINT FK_SURVEYQUESTION_ON_ACTIVITY FOREIGN KEY (activity_id) REFERENCES activity (id);

ALTER TABLE survey_response
    ADD CONSTRAINT FK_SURVEYRESPONSE_ON_QUESTION FOREIGN KEY (question_id) REFERENCES survey_question (id);

ALTER TABLE survey_response
    ADD CONSTRAINT FK_SURVEYRESPONSE_ON_STUDENT FOREIGN KEY (student_id) REFERENCES user (id);

ALTER TABLE activity
    DROP COLUMN activity_type;

ALTER TABLE activity
    ADD activity_type VARCHAR(255) NOT NULL;