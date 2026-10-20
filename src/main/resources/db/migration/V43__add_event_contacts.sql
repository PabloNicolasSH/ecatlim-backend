CREATE TABLE event_contact
(
    id            INT AUTO_INCREMENT NOT NULL,
    event_id      INT                NOT NULL,
    type          VARCHAR(20)        NOT NULL,
    name          VARCHAR(255)       NOT NULL,
    surname       VARCHAR(255)       NOT NULL,
    phone         VARCHAR(255)       NOT NULL,
    email         VARCHAR(255)       NOT NULL,
    origin_entity_id INT             NULL,
    specialty     VARCHAR(255)       NULL,
    CONSTRAINT pk_event_contact PRIMARY KEY (id),
    CONSTRAINT FK_EVENT_CONTACT_ON_ORIGIN_ENTITY FOREIGN KEY (origin_entity_id) REFERENCES scout_group (id) ON DELETE SET NULL,
    CONSTRAINT FK_EVENT_CONTACT_ON_EVENT FOREIGN KEY (event_id) REFERENCES event (id) ON DELETE CASCADE
);
