CREATE TABLE user_education_stage
(
    id                 INT AUTO_INCREMENT NOT NULL,
    user_id            INT                NULL,
    education_stage_id INT                NULL,
    enrollment_date    datetime           NULL,
    completed          BIT(1)             NOT NULL,
    CONSTRAINT pk_usereducationstage PRIMARY KEY (id)
);

ALTER TABLE user_education_stage
    ADD CONSTRAINT FK_USEREDUCATIONSTAGE_ON_EDUCATIONSTAGE FOREIGN KEY (education_stage_id) REFERENCES education_stage (id);

ALTER TABLE user_education_stage
    ADD CONSTRAINT FK_USEREDUCATIONSTAGE_ON_USER FOREIGN KEY (user_id) REFERENCES user (id);