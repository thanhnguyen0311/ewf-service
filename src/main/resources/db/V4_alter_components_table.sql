SET @table_name = 'components';
SET @new_column_name = 'metadata';
SET @drop_column_name = 'price';

-- Check if the new column doesn't exist, and construct the SQL to add it
SELECT IF(
               (SELECT COUNT(*)
                FROM INFORMATION_SCHEMA.COLUMNS
                WHERE TABLE_NAME = @table_name
                  AND TABLE_SCHEMA = DATABASE()
                  AND COLUMN_NAME = @new_column_name
               ) = 0,
               'ALTER TABLE components ADD COLUMN metadata JSON',
               'SELECT "Column metadata already exists";'
       ) INTO @add_column_sql;

-- Execute the statement to add the column
PREPARE stmt FROM @add_column_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Check if the column to be dropped exists, and construct the SQL to drop it
SELECT IF(
               (SELECT COUNT(*)
                FROM INFORMATION_SCHEMA.COLUMNS
                WHERE TABLE_NAME = @table_name
                  AND TABLE_SCHEMA = DATABASE()
                  AND COLUMN_NAME = @drop_column_name
               ) = 1,
               'ALTER TABLE components DROP COLUMN price',
               'SELECT "Column price does not exist";'
       ) INTO @drop_column_sql;

-- Execute the statement to drop the column
PREPARE stmt FROM @drop_column_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;