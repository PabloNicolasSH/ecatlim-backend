ALTER TABLE education_stage ADD previous_stage_id INT NULL;

ALTER TABLE education_stage ADD previous_stage_required BIT(1) NOT NULL;

ALTER TABLE education_stage ADD CONSTRAINT FK_EDUCATIONSTAGE_ON_PREVIOUSSTAGE FOREIGN KEY (previous_stage_id) REFERENCES education_stage (id);

ALTER TABLE module DROP COLUMN learning_hours;

ALTER TABLE module ADD contact_hours INT NOT NULL;

ALTER TABLE module ADD online_hours INT NOT NULL;

ALTER TABLE lesson_block DROP COLUMN hours;

ALTER TABLE lesson_block ADD contact_hours INT NOT NULL;

ALTER TABLE lesson_block ADD online_hours INT NOT NULL;