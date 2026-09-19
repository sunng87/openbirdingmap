ALTER TABLE obm_record RENAME COLUMN specy_id TO species_id;

--;;

ALTER TABLE obm_record ADD COLUMN observer_id TEXT DEFAULT NULL;
