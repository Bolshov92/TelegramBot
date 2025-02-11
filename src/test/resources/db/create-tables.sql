CREATE TABLE IF NOT EXISTS users
(
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    chat_id         BIGINT   NOT NULL UNIQUE,
    first_name      VARCHAR(255),
    last_name       VARCHAR(255),
    username        VARCHAR(255),
    registered_at   DATETIME NOT NULL,
    subscribed_sign VARCHAR(50),
    language        VARCHAR(10)
);
