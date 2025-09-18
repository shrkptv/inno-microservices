--liquibase formatted sql

--changeset shrkptv:1
CREATE TABLE users
(
    id         BIGSERIAL PRIMARY KEY,
    name       VARCHAR(50)         NOT NULL,
    surname    VARCHAR(50)         NOT NULL,
    birth_date DATE                NOT NULL,
    email      VARCHAR(100) UNIQUE NOT NULL
);

--changeset shrkptv:2
CREATE TABLE card_info
(
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT             NOT NULL,
    number          VARCHAR(20) UNIQUE NOT NULL,
    holder          VARCHAR(50)        NOT NULL,
    expiration_date DATE               NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

--changeset shrkptv:3
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_name_surname ON users(name, surname);
CREATE INDEX idx_users_birth_date ON users(birth_date);

CREATE INDEX idx_card_info_user_id ON card_info(user_id);
CREATE INDEX idx_card_info_number ON card_info(number);
CREATE INDEX idx_card_info_expiration_date ON card_info(expiration_date);
CREATE UNIQUE INDEX idx_card_info_user_number ON card_info(user_id, number);