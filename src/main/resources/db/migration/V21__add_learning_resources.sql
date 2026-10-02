CREATE TABLE learning_resource
(
    id            INT AUTO_INCREMENT NOT NULL,
    name          VARCHAR(255)       NULL,
    `description` VARCHAR(255)       NULL,
    blob_path     VARCHAR(255)       NOT NULL,
    resource_type VARCHAR(255)       NULL,
    mime_type     VARCHAR(255)       NOT NULL,
    user_id       INT                NULL,
    created_at    datetime           NULL,
    CONSTRAINT pk_learningresource PRIMARY KEY (id)
);

CREATE TABLE resource_tags
(
    resource_id INT NOT NULL,
    tag_id      INT NOT NULL,
    CONSTRAINT pk_resource_tags PRIMARY KEY (resource_id, tag_id)
);

CREATE TABLE tag
(
    id   INT          NOT NULL,
    name VARCHAR(255) NOT NULL,
    CONSTRAINT pk_tag PRIMARY KEY (id)
);

ALTER TABLE tag
    ADD CONSTRAINT uc_tag_name UNIQUE (name);

ALTER TABLE learning_resource
    ADD CONSTRAINT FK_LEARNINGRESOURCE_ON_USER FOREIGN KEY (user_id) REFERENCES user (id);

ALTER TABLE resource_tags
    ADD CONSTRAINT fk_restag_on_learning_resource FOREIGN KEY (resource_id) REFERENCES learning_resource (id);

ALTER TABLE resource_tags
    ADD CONSTRAINT fk_restag_on_tag FOREIGN KEY (tag_id) REFERENCES tag (id);