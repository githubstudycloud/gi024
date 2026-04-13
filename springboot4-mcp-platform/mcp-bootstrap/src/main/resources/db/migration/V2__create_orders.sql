CREATE TABLE orders (
    id VARCHAR(64) PRIMARY KEY,
    order_no VARCHAR(64) NOT NULL,
    customer_name VARCHAR(128) NOT NULL,
    status VARCHAR(32) NOT NULL,
    amount NUMERIC(18, 2) NOT NULL,
    currency VARCHAR(16) NOT NULL,
    created_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_orders_order_no ON orders (order_no);
CREATE INDEX idx_orders_status ON orders (status);
CREATE INDEX idx_orders_customer_name ON orders (customer_name);
