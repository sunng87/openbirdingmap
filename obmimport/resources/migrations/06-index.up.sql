CREATE INDEX state_code_key ON obm_location (state_code);

--;;

CREATE INDEX locality_species_id_key ON obm_record (locality_id, species_id);
