--liquibase formatted sql

--changeset shrkptv:1
CREATE TABLE auth_users
(
    id BIGSERIAL PRIMARY KEY,
    login VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL
);