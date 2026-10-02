ALTER TABLE user_lesson_block
    DROP FOREIGN KEY FK_USERLESSONBLOCK_ON_EVENT;

ALTER TABLE user_lesson_block
    DROP COLUMN event_id;

ALTER TABLE user_lesson_block
    ADD completion_date datetime NULL;

ALTER TABLE education_session
    ADD lesson_block_id INT NULL;

ALTER TABLE education_session
    ADD CONSTRAINT FK_EDUCATIONSESSION_ON_LESSON_BLOCK FOREIGN KEY (lesson_block_id) REFERENCES lesson_block (id);