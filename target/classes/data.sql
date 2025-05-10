DROP TABLE IF EXISTS commit;
DROP TABLE IF EXISTS project;
CREATE TABLE IF NOT EXISTS commit (
                                      id INT PRIMARY KEY AUTO_INCREMENT,
                                      title VARCHAR(2000)
    );

CREATE TABLE IF NOT EXISTS project (
                                       id VARCHAR(255) PRIMARY KEY,
                                       name VARCHAR(255)
    );

ALTER TABLE commit ALTER COLUMN title SET DATA TYPE VARCHAR(2000);
ALTER TABLE project ALTER COLUMN id VARCHAR(255);
