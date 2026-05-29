ALTER TABLE education_session
    DROP FOREIGN KEY FK_EDUCATIONSESSION_ON_EDUCATOR;

ALTER TABLE education_session DROP COLUMN educator_id;

CREATE TABLE education_session_facilitators (
    education_session_id INT NOT NULL,
    user_id              INT NOT NULL,

    PRIMARY KEY (education_session_id, user_id),

    CONSTRAINT fk_session_facilitator
        FOREIGN KEY (education_session_id)
            REFERENCES education_session (id)
            ON DELETE CASCADE,

    CONSTRAINT fk_user_facilitator
        FOREIGN KEY (user_id)
            REFERENCES user (id)
            ON DELETE CASCADE
)