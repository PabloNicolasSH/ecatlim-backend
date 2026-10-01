CREATE TABLE user_roles
(
    user_id INT          NOT NULL,
    `role`  VARCHAR(255) NOT NULL
);

ALTER TABLE user_roles
    ADD CONSTRAINT fk_user_roles_on_user FOREIGN KEY (user_id) REFERENCES user (id);

ALTER TABLE user
    DROP COLUMN `role`;