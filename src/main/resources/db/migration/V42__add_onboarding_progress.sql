CREATE TABLE onboarding_progress
(
    id           INT AUTO_INCREMENT NOT NULL,
    user_id      INT                NOT NULL,
    progress_key VARCHAR(80)        NOT NULL,
    completed_at datetime           NOT NULL,
    CONSTRAINT pk_onboarding_progress PRIMARY KEY (id),
    CONSTRAINT uc_onboarding_progress_user_key UNIQUE (user_id, progress_key),
    CONSTRAINT FK_ONBOARDING_PROGRESS_ON_USER FOREIGN KEY (user_id) REFERENCES user (id) ON DELETE CASCADE
);
