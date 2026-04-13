CREATE TABLE users (
    id VARCHAR(64) PRIMARY KEY,
    username VARCHAR(64) NOT NULL,
    display_name VARCHAR(128) NOT NULL,
    email VARCHAR(128) NOT NULL,
    role VARCHAR(64) NOT NULL,
    active BOOLEAN NOT NULL,
    created_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_users_username ON users (username);
CREATE INDEX idx_users_role ON users (role);
CREATE INDEX idx_users_active ON users (active);
