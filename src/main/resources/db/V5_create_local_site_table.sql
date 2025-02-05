CREATE TABLE IF NOT EXISTS local
(
    id          INT AUTO_INCREMENT PRIMARY KEY,
    local_sku   VARCHAR(50) NOT NULL UNIQUE,
    local_title text,
    shipping    VARCHAR(50),
    price       DECIMAL(10, 2),
    `order`     INT,
    sold        INT        DEFAULT 0,
    inventory   TINYINT    DEFAULT 0,
    metadata    JSON,
    created_at  TIMESTAMP  DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP  DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted  TINYINT(1) DEFAULT 0
);

