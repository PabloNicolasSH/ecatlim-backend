ALTER TABLE user
    DROP FOREIGN KEY FK_USER_ON_SCOUT_GROUP;

CREATE TABLE user_profile
(
    user_id             INT          NOT NULL,
    name                VARCHAR(255) NOT NULL,
    surname             VARCHAR(255) NOT NULL,
    profile_picture_url VARCHAR(255) NULL,
    nif                 VARCHAR(255) NULL,
    phone               VARCHAR(255) NULL,
    address             VARCHAR(255) NULL,
    city                VARCHAR(255) NULL,
    country             VARCHAR(255) NULL,
    census              INT          NULL,
    scout_group_id      INT          NULL,
    CONSTRAINT pk_userprofile PRIMARY KEY (user_id)
);

ALTER TABLE scout_group
    ADD head_of_education_id INT NULL;

ALTER TABLE scout_group
    ADD CONSTRAINT uc_scoutgroup_head_of_education UNIQUE (head_of_education_id);

ALTER TABLE user
    ADD CONSTRAINT uc_user_email UNIQUE (email);

ALTER TABLE scout_group
    ADD CONSTRAINT FK_SCOUTGROUP_ON_HEAD_OF_EDUCATION FOREIGN KEY (head_of_education_id) REFERENCES user_profile (user_id);

ALTER TABLE user_profile
    ADD CONSTRAINT FK_USERPROFILE_ON_SCOUT_GROUP FOREIGN KEY (scout_group_id) REFERENCES scout_group (id);

ALTER TABLE user_profile
    ADD CONSTRAINT FK_USERPROFILE_ON_USER FOREIGN KEY (user_id) REFERENCES user (id);

ALTER TABLE user
    DROP COLUMN address;

ALTER TABLE user
    DROP COLUMN census;

ALTER TABLE user
    DROP COLUMN city;

ALTER TABLE user
    DROP COLUMN country;

ALTER TABLE user
    DROP COLUMN name;

ALTER TABLE user
    DROP COLUMN nif;

ALTER TABLE user
    DROP COLUMN phone;

ALTER TABLE user
    DROP COLUMN scout_group_id;

ALTER TABLE user
    DROP COLUMN surname;

ALTER TABLE user
    MODIFY email VARCHAR(255) NOT NULL;