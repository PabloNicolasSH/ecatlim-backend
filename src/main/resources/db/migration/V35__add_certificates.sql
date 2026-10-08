ALTER TABLE user_education_stage
    ADD stage_certificate_id INT NULL;

ALTER TABLE user_education_stage
    ADD CONSTRAINT uc_usereducationstage_stage_certificate UNIQUE (stage_certificate_id);

ALTER TABLE user_education_stage
    ADD CONSTRAINT FK_USEREDUCATIONSTAGE_ON_STAGE_CERTIFICATE FOREIGN KEY (stage_certificate_id) REFERENCES user_file (id);
