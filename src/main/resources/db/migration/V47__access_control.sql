ALTER TABLE `user`
    ADD must_change_password BIT      NOT NULL DEFAULT 0,
    ADD password_changed_at  DATETIME NULL,
    ADD totp_secret          VARCHAR(255) NULL,
    ADD totp_enabled         BIT      NOT NULL DEFAULT 0,
    ADD totp_last_step       BIGINT   NULL;

CREATE TABLE refresh_token
(
    id         INT AUTO_INCREMENT NOT NULL,
    user_id    INT                NOT NULL,
    token_hash VARCHAR(64)        NOT NULL,
    created_at DATETIME           NOT NULL,
    expires_at DATETIME           NOT NULL,
    revoked_at DATETIME           NULL,
    CONSTRAINT pk_refreshtoken PRIMARY KEY (id),
    CONSTRAINT uc_refreshtoken_hash UNIQUE (token_hash),
    CONSTRAINT FK_REFRESHTOKEN_ON_USER FOREIGN KEY (user_id) REFERENCES `user` (id) ON DELETE CASCADE
);

CREATE TABLE mfa_recovery_code
(
    id        INT AUTO_INCREMENT NOT NULL,
    user_id   INT                NOT NULL,
    code_hash VARCHAR(64)        NOT NULL,
    used_at   DATETIME           NULL,
    CONSTRAINT pk_mfarecoverycode PRIMARY KEY (id),
    CONSTRAINT uc_mfarecoverycode_user_hash UNIQUE (user_id, code_hash),
    CONSTRAINT FK_MFARECOVERYCODE_ON_USER FOREIGN KEY (user_id) REFERENCES `user` (id) ON DELETE CASCADE
);
