--liquibase formatted sql

--changeset shrkptv:1
CREATE TABLE orders
(
    id            BIGSERIAL PRIMARY KEY,
    user_id       BIGINT      NOT NULL,
    status        VARCHAR(50) NOT NULL,
    creation_date DATE        NOT NULL
);

--changeset shrkptv:2
CREATE TABLE items
(
    id    BIGSERIAL PRIMARY KEY,
    name  VARCHAR(50)   NOT NULL,
    price DECIMAL(8, 2) NOT NULL
);

--changeset shrkptv:3
CREATE TABLE order_items
(
    id       BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL,
    item_id  BIGINT NOT NULL,
    quantity INT    NOT NULL,
    FOREIGN KEY (order_id) REFERENCES orders (id) ON DELETE CASCADE,
    FOREIGN KEY (item_id) REFERENCES items (id)
);

--changeset shrkptv:4
CREATE INDEX idx_orders_status ON orders (status);
