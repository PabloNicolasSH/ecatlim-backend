CREATE TABLE activity_correctors
(
    activity_id INT NOT NULL,
    user_id     INT NOT NULL,
    CONSTRAINT pk_activity_correctors PRIMARY KEY (activity_id, user_id)
);

ALTER TABLE survey_response
    ADD attempt_number INT;

ALTER TABLE survey_response
    ADD response_value VARCHAR(255);

ALTER TABLE activity
    ADD creator_id INT;

ALTER TABLE activity
    ADD lesson_block_id INT;

ALTER TABLE activity
    MODIFY creator_id INT NOT NULL;

ALTER TABLE activity
    MODIFY lesson_block_id INT NOT NULL;

ALTER TABLE activity
    ADD CONSTRAINT FK_ACTIVITY_ON_CREATOR FOREIGN KEY (creator_id) REFERENCES user (id);

ALTER TABLE activity
    ADD CONSTRAINT FK_ACTIVITY_ON_LESSON_BLOCK FOREIGN KEY (lesson_block_id) REFERENCES lesson_block (id);

ALTER TABLE activity_correctors
    ADD CONSTRAINT fk_actcor_on_activity FOREIGN KEY (activity_id) REFERENCES activity (id);

ALTER TABLE activity_correctors
    ADD CONSTRAINT fk_actcor_on_user FOREIGN KEY (user_id) REFERENCES user (id);

ALTER TABLE activity_progress
    DROP COLUMN attempts_count;

ALTER TABLE activity
    DROP COLUMN is_gradable;

ALTER TABLE activity
    DROP COLUMN max_attempts;

ALTER TABLE activity
    DROP COLUMN passing_score;

ALTER TABLE survey_response
    DROP COLUMN num_value;

ALTER TABLE survey_response
    DROP COLUMN text_value;

ALTER TABLE activity_progress
    MODIFY activity_id INT NOT NULL;