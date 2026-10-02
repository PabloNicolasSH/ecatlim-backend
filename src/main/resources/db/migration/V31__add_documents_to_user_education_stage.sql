ALTER TABLE user_education_stage
    ADD personal_plan_id INT NULL;

ALTER TABLE user_education_stage
    ADD entity_approval_id INT NULL;

ALTER TABLE user_education_stage
    ADD CONSTRAINT uc_usereducationstage_personal_plan UNIQUE (personal_plan_id);

ALTER TABLE user_education_stage
    ADD CONSTRAINT uc_usereducationstage_entity_approval UNIQUE (entity_approval_id);

ALTER TABLE user_education_stage
    ADD CONSTRAINT FK_USEREDUCATIONSTAGE_ON_PERSONAL_PLAN FOREIGN KEY (personal_plan_id) REFERENCES user_file (id);

ALTER TABLE user_education_stage
    ADD CONSTRAINT FK_USEREDUCATIONSTAGE_ON_ENTITY_APPROVAL FOREIGN KEY (entity_approval_id) REFERENCES user_file (id);
