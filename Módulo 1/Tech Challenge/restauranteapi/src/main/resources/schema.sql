CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    login VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(512) NOT NULL,
    address VARCHAR(255) NOT NULL,
    type VARCHAR(50) NOT NULL,
    last_modified_at TIMESTAMP NOT NULL
);

UPDATE users SET type = 'CLIENTE' WHERE type = '1';
UPDATE users SET type = 'DONO_RESTAURANTE' WHERE type = '2';
UPDATE users SET last_modified_at = CURRENT_TIMESTAMP WHERE last_modified_at IS NULL;
