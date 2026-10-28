ALTER TABLE `user`
    ADD last_login_at DATETIME NULL;

-- The 3 month clock of the passwords starts now for everyone who never changed theirs
UPDATE `user`
SET password_changed_at = NOW()
WHERE password_changed_at IS NULL;
