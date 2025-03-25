ALTER DATABASE CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;

CREATE TABLE education_stage
(
    id          INT AUTO_INCREMENT NOT NULL,
    name        VARCHAR(255)       NOT NULL,
    code        VARCHAR(255)       NOT NULL,
    description VARCHAR(1000)      NOT NULL,
    CONSTRAINT pk_education_stage PRIMARY KEY (id)
);

CREATE TABLE event
(
    id                INT AUTO_INCREMENT NOT NULL,
    title             VARCHAR(255)       NOT NULL,
    start_date        datetime           NOT NULL,
    end_date          datetime           NOT NULL,
    location          VARCHAR(255)       NOT NULL,
    theoretical_hours INT                NULL,
    practical_hours   INT                NULL,
    online_hours      INT                NULL,
    director_id       INT                NULL,
    organizer         VARCHAR(255)       NOT NULL,
    CONSTRAINT pk_event PRIMARY KEY (id)
);

CREATE TABLE lesson_block
(
    id              INT AUTO_INCREMENT NOT NULL,
    name            VARCHAR(255)       NOT NULL,
    lesson_block_id INT                NOT NULL,
    description     VARCHAR(1000)      NOT NULL,
    hours           INT                NOT NULL,
    recognizable    BIT(1)             NOT NULL,
    module_id       INT                NULL,
    CONSTRAINT pk_lesson_block PRIMARY KEY (id)
);

CREATE TABLE module
(
    id                 INT AUTO_INCREMENT NOT NULL,
    name               VARCHAR(255)       NOT NULL,
    description        VARCHAR(1000)      NOT NULL,
    type               VARCHAR(255)       NOT NULL,
    learning_hours     INT                NULL,
    education_stage_id INT                NULL,
    CONSTRAINT pk_module PRIMARY KEY (id)
);

CREATE TABLE scout_group
(
    id           INT AUTO_INCREMENT NOT NULL,
    name         VARCHAR(255)       NOT NULL,
    province_id  INT                NOT NULL,
    group_number INT                NOT NULL,
    CONSTRAINT pk_scout_group PRIMARY KEY (id)
);

CREATE TABLE user
(
    id             INT AUTO_INCREMENT NOT NULL,
    name           VARCHAR(255)       NOT NULL,
    surname        VARCHAR(255)       NOT NULL,
    role           VARCHAR(255)       NOT NULL,
    password       VARCHAR(255)       NOT NULL,
    nif            VARCHAR(255)       NULL,
    email          VARCHAR(255)       NULL,
    phone          VARCHAR(255)       NULL,
    address        VARCHAR(255)       NULL,
    city           VARCHAR(255)       NULL,
    country        VARCHAR(255)       NULL,
    census         INT                NULL,
    scout_group_id INT                NULL,
    enabled        BIT(1)             NOT NULL,
    CONSTRAINT pk_user PRIMARY KEY (id)
);

CREATE TABLE user_lesson_block
(
    id              INT AUTO_INCREMENT NOT NULL,
    lesson_block_id INT                NULL,
    user_id         INT                NULL,
    completed       BIT(1)             NOT NULL,
    enrollment_date datetime           NULL,
    CONSTRAINT pk_user_lesson_block PRIMARY KEY (id)
);

ALTER TABLE event
    ADD CONSTRAINT FK_EVENT_ON_DIRECTOR FOREIGN KEY (director_id) REFERENCES user (id);

ALTER TABLE lesson_block
    ADD CONSTRAINT FK_LESSON_BLOCK_ON_MODULE FOREIGN KEY (module_id) REFERENCES module (id);

ALTER TABLE module
    ADD CONSTRAINT FK_MODULE_ON_EDUCATION_STAGE FOREIGN KEY (education_stage_id) REFERENCES education_stage (id);

ALTER TABLE user_lesson_block
    ADD CONSTRAINT FK_USER_LESSON_BLOCK_ON_LESSON_BLOCK FOREIGN KEY (lesson_block_id) REFERENCES lesson_block (id);

ALTER TABLE user_lesson_block
    ADD CONSTRAINT FK_USER_LESSON_BLOCK_ON_USER FOREIGN KEY (user_id) REFERENCES user (id);

ALTER TABLE user
    ADD CONSTRAINT FK_USER_ON_SCOUT_GROUP FOREIGN KEY (scout_group_id) REFERENCES scout_group (id);