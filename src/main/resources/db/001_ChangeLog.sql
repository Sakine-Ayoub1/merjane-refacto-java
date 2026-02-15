-- liquibase formatted sql

-- changeset ayoub:1
DROP TABLE IF EXISTS products;
CREATE TABLE products (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    lead_time INT,
    available INT,
    type VARCHAR(50),
    name VARCHAR(255),
    expiry_date DATE,
    season_start_date DATE,
    season_end_date DATE
);

-- changeset ayoub:2
DROP TABLE IF EXISTS orders;
CREATE TABLE orders (
     id BIGINT AUTO_INCREMENT PRIMARY KEY
);

-- changeset ayoub:3
DROP TABLE IF EXISTS order_items;
CREATE TABLE order_items (
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    PRIMARY KEY (order_id, product_id),
    CONSTRAINT fk_order_items_orders FOREIGN KEY (order_id) REFERENCES orders(id),
    CONSTRAINT fk_order_items_products FOREIGN KEY (product_id) REFERENCES products(id)
);