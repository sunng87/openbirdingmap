ALTER TABLE obm_record RENAME COLUMN specy_id TO species_id;

--;;

ALTER TABLE obm_record ADD COLUMN observer_id VARCHAR(36) DEFAULT NULL;
