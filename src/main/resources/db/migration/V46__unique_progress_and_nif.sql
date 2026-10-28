-- Progress left behind by users who unenrolled from a block before unenrolling cleaned it up
DELETE p
FROM activity_progress p
         JOIN activity a ON a.id = p.activity_id
WHERE a.event_id IS NOT NULL
  AND NOT EXISTS (SELECT 1
                  FROM event_enrollment ee
                  WHERE ee.event_id = a.event_id
                    AND ee.lesson_block_id = a.lesson_block_id
                    AND ee.user_id = p.student_id);

-- Keep a single progress row per activity and student
DELETE p
FROM activity_progress p
         JOIN activity_progress keep
              ON keep.activity_id = p.activity_id AND keep.student_id = p.student_id AND keep.id < p.id;

ALTER TABLE activity_progress
    ADD CONSTRAINT uc_activityprogress_activity_student UNIQUE (activity_id, student_id);

ALTER TABLE user_profile
    ADD CONSTRAINT uc_userprofile_nif UNIQUE (nif);
