CREATE DATABASE IF NOT EXISTS ewf;


CREATE TABLE IF NOT EXISTS products
(
    id               INT AUTO_INCREMENT PRIMARY KEY,
    sku              VARCHAR(50)                                                     NOT NULL UNIQUE,
    upc              VARCHAR(50),
    name             VARCHAR(255),
    type             ENUM ('table', 'chair', 'set', 'bed', 'chest', 'bunk', 'floor') NOT NULL,
    shipping         ENUM ('LTL', 'GND'),
    category         ENUM ('Floor', 'Indoor','Outdoor' ),
    sub_category     VARCHAR(100),
    finish           VARCHAR(50),
    product_type     VARCHAR(50),
    price            DECIMAL(10, 2),
    sold             INT       DEFAULT 0,
    inventory        TINYINT   DEFAULT 0,
    description      TEXT,
    html_description TEXT,
    images           JSON,
    dims_image       VARCHAR(255),
    pdf              VARCHAR(255),
    metadata         JSON,
    created_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted       BOOLEAN
);


CREATE TABLE IF NOT EXISTS components
(
    id         INT AUTO_INCREMENT PRIMARY KEY,
    sku        VARCHAR(50) NOT NULL,
    name       VARCHAR(255),
    quantity   INT         NOT NULL DEFAULT 1,
    box        TINYINT     NOT NULL DEFAULT 1,
    dims       VARCHAR(50),
    box_dims   VARCHAR(50),
    type       VARCHAR(50),
    price      DECIMAL(10, 2),
    created_at TIMESTAMP            DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP            DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted BOOLEAN
);


CREATE TABLE IF NOT EXISTS product_components
(
    id           INT AUTO_INCREMENT PRIMARY KEY,
    product_id   INT NOT NULL,
    component_id INT NOT NULL,

    FOREIGN KEY (product_id) REFERENCES products (id),
    FOREIGN KEY (component_id) REFERENCES components (id) ON DELETE CASCADE,

    CONSTRAINT unique_product_component UNIQUE (product_id, component_id)
);

CREATE TABLE IF NOT EXISTS set_details
(
    id         INT AUTO_INCREMENT PRIMARY KEY,
    set_id     INT NOT NULL,
    product_id INT NOT NULL,
    quantity   INT NOT NULL,

    FOREIGN KEY (set_id) REFERENCES products (id),
    FOREIGN KEY (product_id) REFERENCES products (id) ON DELETE CASCADE,
    CONSTRAINT unique_set_product UNIQUE (set_id, product_id)

);