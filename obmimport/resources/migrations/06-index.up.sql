ALTER TABLE obm_location ADD INDEX state_code_key (state_code);

--;;

ALTER TABLE obm_record ADD INDEX locality_species_id_key (locality_id, species_id);
