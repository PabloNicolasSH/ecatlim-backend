ALTER TABLE event_enrollment
    ADD attendance VARCHAR(30) NULL;

UPDATE event_enrollment
SET attendance = 'TOTAL'
WHERE has_attended = TRUE;

ALTER TABLE event_enrollment
    DROP COLUMN has_attended;

ALTER TABLE activity
    ADD assigned_user_id INT NULL;

ALTER TABLE activity
    ADD CONSTRAINT FK_ACTIVITY_ON_ASSIGNED_USER FOREIGN KEY (assigned_user_id) REFERENCES user (id) ON DELETE CASCADE;
