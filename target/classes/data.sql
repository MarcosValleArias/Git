CREATE TABLE IF NOT EXISTS commit (
                                      id INT PRIMARY KEY AUTO_INCREMENT,
                                      title VARCHAR(255)
    );
CREATE TABLE IF NOT EXISTS project (
                                       id VARCHAR(255) PRIMARY KEY,
                                       name VARCHAR(255)
    );

ALTER TABLE commit ALTER COLUMN title SET DATA TYPE VARCHAR(500);
ALTER TABLE project ALTER COLUMN id VARCHAR(255);
