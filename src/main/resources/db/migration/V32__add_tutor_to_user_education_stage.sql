ALTER TABLE user_education_stage
    ADD tutor_id INT NULL;

ALTER TABLE user_education_stage
    ADD CONSTRAINT FK_USEREDUCATIONSTAGE_ON_TUTOR FOREIGN KEY (tutor_id) REFERENCES user (id);
