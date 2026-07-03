CREATE TABLE event_configuration
(
    id                     INT AUTO_INCREMENT NOT NULL,
    min_participants       INT                NOT NULL,
    date_open_inscription  datetime           NOT NULL,
    date_close_inscription datetime           NOT NULL,
    cost                   INT                NOT NULL,
    transfer_bank_number   VARCHAR(255)       NOT NULL,
    transfer_code          VARCHAR(255)       NOT NULL,
    notification_target    VARCHAR(255)       NOT NULL,
    CONSTRAINT pk_eventconfiguration PRIMARY KEY (id)
);

CREATE TABLE event_facilitators
(
    event_id INT NOT NULL,
    user_id  INT NOT NULL,
    CONSTRAINT pk_event_facilitators PRIMARY KEY (event_id, user_id)
);

CREATE TABLE event_staff
(
    event_id INT NOT NULL,
    user_id  INT NOT NULL,
    CONSTRAINT pk_event_staff PRIMARY KEY (event_id, user_id)
);

ALTER TABLE event
    ADD contents TEXT NOT NULL;

ALTER TABLE event
    ADD education_stage_id INT;

ALTER TABLE event
    ADD event_configuration_id INT;

ALTER TABLE event
    ADD shortname VARCHAR(255) NOT NULL NULL;

ALTER TABLE event
    ADD status VARCHAR(255) NOT NULL NULL;

ALTER TABLE event
    MODIFY contents TEXT NOT NULL;

ALTER TABLE event
    ADD CONSTRAINT FK_EVENT_ON_EDUCATION_STAGE FOREIGN KEY (education_stage_id) REFERENCES education_stage (id);

ALTER TABLE event
    ADD CONSTRAINT FK_EVENT_ON_EVENT_CONFIGURATION FOREIGN KEY (event_configuration_id) REFERENCES event_configuration (id);

ALTER TABLE event_facilitators
    ADD CONSTRAINT fk_evefac_on_event FOREIGN KEY (event_id) REFERENCES event (id);

ALTER TABLE event_facilitators
    ADD CONSTRAINT fk_evefac_on_user FOREIGN KEY (user_id) REFERENCES user (id);

ALTER TABLE event_staff
    ADD CONSTRAINT fk_evesta_on_event FOREIGN KEY (event_id) REFERENCES event (id);

ALTER TABLE event_staff
    ADD CONSTRAINT fk_evesta_on_user FOREIGN KEY (user_id) REFERENCES user (id);