SET @table_name = 'products';
SET @column_name = 'local_title';

-- Dynamically construct the ALTER TABLE statement if the column doesn't exist
SELECT IF(
               (SELECT COUNT(*)
                FROM INFORMATION_SCHEMA.COLUMNS
                WHERE TABLE_NAME = @table_name
                  AND TABLE_SCHEMA = 'ewf'
                  AND COLUMN_NAME = @column_name
               ) = 0,
               'ALTER TABLE products ADD COLUMN local_title TEXT',
               'SELECT "Column already exists";'
       ) INTO @sql;

-- Execute the statement
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
