ALTER TABLE obm_location ADD COLUMN (
country VARCHAR(64) NOT NULL,
country_code VARCHAR(12) NOT NULL,
state_name VARCHAR(128) NOT NULL,
state_code VARCHAR(56) NOT NULL
);
