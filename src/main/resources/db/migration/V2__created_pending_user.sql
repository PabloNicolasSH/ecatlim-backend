CREATE TABLE pending_user
(
    id             INT AUTO_INCREMENT NOT NULL,
    name           VARCHAR(255) NOT NULL,
    surname        VARCHAR(255) NOT NULL,
    email          VARCHAR(255) NOT NULL,
    scout_group_id INT          NULL,
    accepted       BIT(1)       NOT NULL,
    CONSTRAINT pk_pendinguser PRIMARY KEY (id)
);

ALTER TABLE pending_user
    ADD CONSTRAINT FK_PENDINGUSER_ON_SCOUTGROUP FOREIGN KEY (scout_group_id) REFERENCES scout_group (id);