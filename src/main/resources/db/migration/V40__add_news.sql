CREATE TABLE news
(
    id              INT AUTO_INCREMENT NOT NULL,
    title           VARCHAR(150)       NOT NULL,
    content         VARCHAR(5000)      NOT NULL,
    image_blob_path VARCHAR(255)       NULL,
    image_mime_type VARCHAR(100)       NULL,
    author_id       INT                NULL,
    created_at      datetime           NOT NULL,
    updated_at      datetime           NULL,
    CONSTRAINT pk_news PRIMARY KEY (id)
);

ALTER TABLE news
    ADD CONSTRAINT FK_NEWS_ON_AUTHOR FOREIGN KEY (author_id) REFERENCES user (id) ON DELETE SET NULL;

CREATE INDEX idx_news_created ON news (created_at);

CREATE TABLE news_attachment
(
    id         INT AUTO_INCREMENT NOT NULL,
    news_id    INT                NOT NULL,
    name       VARCHAR(255)       NOT NULL,
    blob_path  VARCHAR(255)       NOT NULL,
    mime_type  VARCHAR(100)       NOT NULL,
    size_bytes BIGINT             NOT NULL,
    CONSTRAINT pk_news_attachment PRIMARY KEY (id)
);

ALTER TABLE news_attachment
    ADD CONSTRAINT FK_NEWS_ATTACHMENT_ON_NEWS FOREIGN KEY (news_id) REFERENCES news (id) ON DELETE CASCADE;
