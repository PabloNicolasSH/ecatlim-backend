ALTER TABLE education_stage
    ADD previous_stage_id INT NULL;

ALTER TABLE education_stage
    ADD previous_stage_required BIT(1) NULL;

ALTER TABLE education_stage
    MODIFY previous_stage_required BIT (1) NOT NULL;

ALTER TABLE education_stage
    ADD CONSTRAINT FK_EDUCATIONSTAGE_ON_PREVIOUSSTAGE FOREIGN KEY (previous_stage_id) REFERENCES education_stage (id);