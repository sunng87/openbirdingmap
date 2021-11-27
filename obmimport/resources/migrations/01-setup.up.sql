CREATE TABLE obm_location (
  id VARCHAR(64) PRIMARY KEY,
  lname VARCHAR(255) NOT NULL,
  lon double NOT NULL,
  lat double NOT NULL
) CHARACTER SET 'utf8mb4' COLLATE=utf8mb4_unicode_ci;

--;;

CREATE TABLE obm_species (
  id VARCHAR(64) PRIMARY KEY,
  cname VARCHAR(128) NOT NULL,
  sname VARCHAR(128) NOT NULL
) CHARACTER SET 'utf8mb4' COLLATE=utf8mb4_unicode_ci;

--;;

CREATE TABLE obm_record (
  id VARCHAR(128) PRIMARY KEY,
  specy_id VARCHAR(64) NOT NULL,
  locality_id VARCHAR(64) NOT NULL,
  record_date timestamp NOT NULL,
  record_count INT
) CHARACTER SET 'utf8mb4' COLLATE=utf8mb4_unicode_ci;
