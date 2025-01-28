CREATE DATABASE IF NOT EXISTS ewf;
USE ewf;



CREATE TABLE chair_types (
                             id INT AUTO_INCREMENT PRIMARY KEY,
                             name VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE chairs (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        sku VARCHAR(50) NOT NULL UNIQUE,
                        type_id INT NOT NULL,
                        finish VARCHAR(50) NOT NULL,
                        price DECIMAL(10, 2) NOT NULL,
                        quantity INT NOT NULL,
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        FOREIGN KEY (type_id) REFERENCES chair_types(id)
);
CREATE TABLE table_types (
                             id INT AUTO_INCREMENT PRIMARY KEY,
                             name VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE tables (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        sku VARCHAR(50) NOT NULL UNIQUE,
                        type_id INT NOT NULL,
                        finish VARCHAR(50) NOT NULL,
                        material VARCHAR(100) NOT NULL,
                        price DECIMAL(10, 2) NOT NULL,
                        quantity INT NOT NULL,
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        FOREIGN KEY (type_id) REFERENCES table_types(id) ON DELETE CASCADE
);

CREATE TABLE table_chair_sets (
                                  id INT AUTO_INCREMENT PRIMARY KEY,
                                  sku VARCHAR(50) NOT NULL UNIQUE,
                                  price DECIMAL(10, 2) NOT NULL,
                                  quantity INT NOT NULL DEFAULT 0,
                                  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE table_chair_set_details (
                                         id INT AUTO_INCREMENT PRIMARY KEY,
                                         set_id INT NOT NULL,
                                         chair_id INT,
                                         table_id INT,
                                         chair_quantity INT,
                                         table_quantity INT,

                                         FOREIGN KEY (set_id) REFERENCES table_chair_sets(id),
                                         FOREIGN KEY (chair_id) REFERENCES chairs(id),
                                         FOREIGN KEY (table_id) REFERENCES tables(id),

                                         CONSTRAINT unique_set_chair_table UNIQUE (set_id, chair_id, table_id)
);