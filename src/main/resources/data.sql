-- Spring Boot will automatically pick up the data.sql and run
-- it against our configured H2 database during application startup.
-- This is a good way to seed the database for testing or other purposes

DROP TABLE IF EXISTS person;
CREATE TABLE person (
  id INT AUTO_INCREMENT PRIMARY KEY,
  first_name VARCHAR(250) NOT NULL,
  last_name VARCHAR(250) NOT NULL
);

INSERT INTO person (first_name, last_name) VALUES
  ('Aliko', 'Dangote'),
  ('Bill', 'Gates'),
  ('Folrunsho', 'Alakija');