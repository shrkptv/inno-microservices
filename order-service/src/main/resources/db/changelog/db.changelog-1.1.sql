--liquibase formatted sql

--changeset shrkptv:1
ALTER TABLE order_items DROP CONSTRAINT fk_order_items_order;

--changeset shrkptv:2
ALTER TABLE order_items
ADD CONSTRAINT fk_order_items_order
FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE;
