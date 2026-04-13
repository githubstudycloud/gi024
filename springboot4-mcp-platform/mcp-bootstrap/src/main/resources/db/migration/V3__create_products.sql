CREATE TABLE products (
    id VARCHAR(64) PRIMARY KEY,
    product_code VARCHAR(64) NOT NULL,
    name VARCHAR(128) NOT NULL,
    category VARCHAR(64) NOT NULL,
    price NUMERIC(18, 2) NOT NULL,
    currency VARCHAR(16) NOT NULL,
    active BOOLEAN NOT NULL,
    created_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_products_product_code ON products (product_code);
CREATE INDEX idx_products_name ON products (name);
CREATE INDEX idx_products_category ON products (category);
