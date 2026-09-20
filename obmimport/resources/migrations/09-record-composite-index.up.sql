-- composite index covering the species-page queries:
--  - weekly stats: species_id = ? AND locality_id IN (...) (record_date makes it covering)
--  - other-localities: GROUP BY locality_id in index order, no temp b-tree
-- its leftmost column also covers species_id_key, which is therefore dropped
CREATE INDEX species_locality_date_key ON obm_record (species_id, locality_id, record_date);

--;;

DROP INDEX species_id_key;
