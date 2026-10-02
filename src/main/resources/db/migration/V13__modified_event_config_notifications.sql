CREATE TABLE event_configuration_notifications
(
    event_configuration_id INT          NOT NULL,
    notification_target    VARCHAR(255) NOT NULL
);

ALTER TABLE event_configuration_notifications
    ADD CONSTRAINT fk_event_configuration_notifications_on_event_configuration FOREIGN KEY (event_configuration_id) REFERENCES event_configuration (id);

ALTER TABLE event_configuration
    DROP COLUMN notification_target;