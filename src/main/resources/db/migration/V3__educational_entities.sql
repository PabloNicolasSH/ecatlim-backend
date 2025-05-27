CREATE TABLE activity
(
    id            INT AUTO_INCREMENT NOT NULL,
    title         VARCHAR(255)       NOT NULL,
    description VARCHAR(255)       NOT NULL,
    activity_type SMALLINT           NOT NULL,
    is_optional   BIT(1)             NULL,
    CONSTRAINT pk_activity PRIMARY KEY (id)
);

CREATE TABLE education_session
(
    id            INT AUTO_INCREMENT NOT NULL,
    hours         INT                NOT NULL,
    title         VARCHAR(255)       NOT NULL,
    description VARCHAR(255)       NOT NULL,
    educator_id   INT                NULL,
    CONSTRAINT pk_educationsession PRIMARY KEY (id)
);

CREATE TABLE education_session_activities
(
    education_session_id INT NOT NULL,
    activities_id        INT NOT NULL
);

CREATE TABLE event_education_sessions
(
    event_id              INT NOT NULL,
    education_sessions_id INT NOT NULL
);

CREATE TABLE user_education_stages
(
    user_id             INT NOT NULL,
    education_stages_id INT NOT NULL
);

ALTER TABLE user_lesson_block ADD event_id INT NULL;

ALTER TABLE education_session_activities ADD CONSTRAINT uc_education_session_activities_activities UNIQUE (activities_id);

ALTER TABLE event_education_sessions ADD CONSTRAINT uc_event_education_sessions_educationsessions UNIQUE (education_sessions_id);

ALTER TABLE education_session ADD CONSTRAINT FK_EDUCATIONSESSION_ON_EDUCATOR FOREIGN KEY (educator_id) REFERENCES user (id);

ALTER TABLE user_lesson_block ADD CONSTRAINT FK_USERLESSONBLOCK_ON_EVENT FOREIGN KEY (event_id) REFERENCES event (id);

ALTER TABLE education_session_activities ADD CONSTRAINT fk_edusesact_on_activity FOREIGN KEY (activities_id) REFERENCES activity (id);

ALTER TABLE education_session_activities ADD CONSTRAINT fk_edusesact_on_education_session FOREIGN KEY (education_session_id) REFERENCES education_session (id);

ALTER TABLE event_education_sessions ADD CONSTRAINT fk_eveeduses_on_education_session FOREIGN KEY (education_sessions_id) REFERENCES education_session (id);

ALTER TABLE event_education_sessions ADD CONSTRAINT fk_eveeduses_on_event FOREIGN KEY (event_id) REFERENCES event (id);

ALTER TABLE user_education_stages ADD CONSTRAINT fk_useedusta_on_education_stage FOREIGN KEY (education_stages_id) REFERENCES education_stage (id);

ALTER TABLE user_education_stages ADD CONSTRAINT fk_useedusta_on_user FOREIGN KEY (user_id) REFERENCES user (id);

ALTER TABLE user MODIFY scout_group_id INT NOT NULL;