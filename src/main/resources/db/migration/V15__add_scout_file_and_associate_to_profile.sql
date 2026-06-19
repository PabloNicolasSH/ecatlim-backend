CREATE TABLE user_file
(
    id          INT AUTO_INCREMENT NOT NULL,
    uuid        VARCHAR(255)       NOT NULL,
    name        VARCHAR(255)       NOT NULL,
    file_type   SMALLINT           NOT NULL,
    mime_type   VARCHAR(255)       NOT NULL,
    custom_name VARCHAR(255)       NULL,
    upload_date datetime           NOT NULL,
    CONSTRAINT pk_userfile PRIMARY KEY (id)
);

ALTER TABLE user_profile
    ADD profile_picture_id INT NULL;

ALTER TABLE user_profile
    ADD CONSTRAINT uc_userprofile_profile_picture UNIQUE (profile_picture_id);

ALTER TABLE user_profile
    ADD CONSTRAINT FK_USERPROFILE_ON_PROFILE_PICTURE FOREIGN KEY (profile_picture_id) REFERENCES user_file (id);

ALTER TABLE user_profile
    DROP COLUMN profile_picture_url;